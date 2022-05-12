package fr.gouv.stopc.submissioncode.controller

import fr.gouv.stopc.submissioncode.csvOutputFileFormatSpecTest
import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.LongCodesCsvFile
import fr.gouv.stopc.submissioncode.test.SftpManager
import fr.gouv.stopc.submissioncode.test.readTarGzEntries
import io.restassured.RestAssured.given
import io.restassured.http.ContentType.JSON
import org.awaitility.Awaitility.await
import org.awaitility.pollinterval.FibonacciPollInterval.fibonacci
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.everyItem
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.matchesPattern
import org.hamcrest.Matchers.matchesRegex
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.OK
import org.springframework.jdbc.core.JdbcTemplate
import java.time.OffsetDateTime.now
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.temporal.ChronoUnit.DAYS
import java.util.TimeZone

@IntegrationTest
class LongCodesGeneratorTest(@Autowired val jdbcTemplate: JdbcTemplate) {

    @Test
    fun should_send_1_archive_containing_100_codes_for_the_next_day() {
        given()
            .contentType(JSON)
            .body(
                mapOf(
                    "from" to now(UTC).truncatedTo(DAYS).plusDays(1).toString(),
                    "to" to now(UTC).truncatedTo(DAYS).plusDays(2).minusNanos(1).toString(),
                    "dailyAmount" to 100
                )
            )
            .post("/api/v1/back-office/codes/generate/request")
            .then()
            .statusCode(OK.value())

        await("a lot_keys to be available")
            .pollInterval(fibonacci())
            .until(
                { jdbcTemplate.queryForObject("select count(*) from lot_keys", Int::class.java) },
                greaterThan(0)
            )

        val generatedFiles = SftpManager.listFiles()

        assertThat(
            "Files names on SFTP server", generatedFiles.map { it.name },
            allOf(
                contains(
                    matchesRegex("^\\d{14}_stopcovid_qrcode_batch\\.sha256$"),
                    matchesRegex("^\\d{14}_stopcovid_qrcode_batch\\.tgz$"),
                )
            )
        )

        val csvFiles = readTarGzEntries(generatedFiles[1].content)
        assertThat("exactly 1 CSV file has been generated", csvFiles, hasSize(1))

        val todayYYMMDD = now().plusDays(1).format(DateTimeFormatter.ofPattern("yyMMdd"))
        assertThat("CSV file is named [seq number][yyMMdd]", csvFiles[0].name, matchesPattern("\\d+$todayYYMMDD.csv"))

        val expectedStartDate = now().truncatedTo(DAYS)
            .plusDays(1)
            .format(ISO_LOCAL_DATE) + "T00:00:00.000Z"
        val expectedEndDate = now().truncatedTo(DAYS)
            .plusDays(8)
            .atZoneSameInstant(TimeZone.getTimeZone("Europe/Paris").toZoneId())
            .withHour(23).withMinute(59)
            .toInstant()
            .toString()
            .replace(":00Z", ":00.000Z")
        val csvFile = csvFiles.map { LongCodesCsvFile(it.content.decodeToString()) }
            .single()
        assertThat("CSV header", csvFile.header, equalTo("code_pour_qr, code_brut, validite_debut, validite_fin"))
        assertThat(
            "CSV values are consistent", csvFile,
            everyItem(
                allOf(
                    hasProperty("link", matchesPattern("^https://app.stopcovid.gouv.fr\\?code=[a-z0-9-]{36}&type=1$")),
                    hasProperty("code", matchesPattern("^[a-z0-9-]{36}$")),
                    hasProperty("start", equalTo(expectedStartDate)),
                    hasProperty("end", equalTo(expectedEndDate))
                )
            )
        )
        assertThat("The number of generated codes", csvFile.count(), equalTo(100))
    }

    @TestFactory
    fun should_generate_valid_csv_files(): List<DynamicContainer> {
        given()
            .contentType(JSON)
            .body(
                mapOf(
                    "from" to now(UTC).truncatedTo(DAYS).plusDays(1).toString(),
                    "to" to now(UTC).truncatedTo(DAYS).plusDays(2).minusNanos(1).toString(),
                    "dailyAmount" to 500
                )
            )
            .post("/api/v1/back-office/codes/generate/request")

        await("a lot_keys to be available")
            .pollInterval(fibonacci())
            .until(
                { jdbcTemplate.queryForObject("select count(*) from lot_keys", Int::class.java) },
                greaterThan(0)
            )

        return SftpManager.listFiles()
            .filter { it.name.endsWith(".tgz") }
            .map {
                dynamicContainer(
                    "archive ${it.name} contains",
                    readTarGzEntries(it.content).map { csvFile ->
                        dynamicContainer("a csv file ${csvFile.name}", csvOutputFileFormatSpecTest(csvFile, startDateZoneId = UTC))
                    }
                )
            }
    }
}
