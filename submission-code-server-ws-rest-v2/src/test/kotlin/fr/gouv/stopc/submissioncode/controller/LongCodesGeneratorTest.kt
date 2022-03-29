package fr.gouv.stopc.submissioncode.controller

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
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.everyItem
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.matchesPattern
import org.hamcrest.Matchers.matchesRegex
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.startsWith
import org.hamcrest.Matchers.stringContainsInOrder
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDate
import java.time.OffsetDateTime.now
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.temporal.ChronoUnit.DAYS

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

        val expectedStartDate = now().plusDays(1).truncatedTo(DAYS)
            .format(ISO_LOCAL_DATE) + "T00:00:00.000Z"
        val expectedEndDate = now().plusDays(9).truncatedTo(DAYS).minusMinutes(1)
            .format(ISO_LOCAL_DATE) + "T21:59:00.000Z"
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

    @Nested
    @TestInstance(PER_CLASS)
    inner class CsvOutputFileFormatSpec {

        private lateinit var csvFile: String

        private lateinit var lines: List<List<String>>

        @BeforeAll
        fun generate_a_csv_file(@Autowired jdbcTemplate: JdbcTemplate) {
            given()
                .contentType(JSON)
                .body(
                    mapOf(
                        "from" to now(UTC).truncatedTo(DAYS).plusDays(1).toString(),
                        "to" to now(UTC).truncatedTo(DAYS).plusDays(3).minusNanos(1).toString(),
                        "dailyAmount" to 45000
                    )
                )
                .post("/api/v1/back-office/codes/generate/request")

            await("a lot_keys to be available")
                .pollInterval(fibonacci())
                .until(
                    { jdbcTemplate.queryForObject("select count(*) from lot_keys", Int::class.java) },
                    greaterThan(0)
                )

            csvFile = SftpManager.listFiles()
                .filter { it.name.endsWith(".tgz") }
                .flatMap { readTarGzEntries(it.content) }
                .single()
                .content.decodeToString()

            lines = csvFile
                .removeSuffix("\n")
                .lines()
                .drop(1)
                .map { it.split(",") }
        }

        @Test
        fun header_has_4_unquoted_columns() {
            assertThat(csvFile, startsWith("code_pour_qr, code_brut, validite_debut, validite_fin\n"))
        }

        @Test
        fun ends_with_an_empty_line() {
            assertThat(csvFile.lines().last(), equalTo(""))
        }

        @Test
        fun all_lines_ends_with_lf() {
            assertThat(csvFile, not(stringContainsInOrder("\r\n")))
        }

        @Test
        fun lines_have_4_values_delimited_by_comma() {
            assertThat(lines, everyItem(hasSize(4)))
        }

        @Test
        fun values_are_surrounded_with_double_quotes() {
            val values = lines.flatten()
            assertThat(
                values,
                everyItem(
                    allOf(
                        startsWith("\""),
                        endsWith("\"")
                    )
                )
            )
        }

        @Test
        fun column_1_contains_an_url() {
            val firstColumn = lines.map { unquote(it[0]) }
            assertThat(
                firstColumn,
                everyItem(
                    allOf(
                        startsWith("https://app.stopcovid.gouv.fr?code="),
                        endsWith("&type=1"),
                        matchesPattern("^https://app.stopcovid.gouv.fr\\?code=[a-z0-9-]{36}&type=1\$")
                    )
                )
            )
        }

        @Test
        fun column_2_contains_a_code() {
            val firstColumn = lines.map { unquote(it[1]) }
            assertThat(firstColumn, everyItem(matchesPattern("[a-z0-9-]{36}")))
        }

        @Test
        fun column_3_contains_the_validity_start_date() {
            val startDay = LocalDate.now().plusDays(1)
            val firstColumn = lines.map { unquote(it[2]) }
            assertThat(firstColumn, everyItem(equalTo("${startDay}T00:00:00.000Z")))
        }

        @Test
        fun `column_4_contains_the_validity_end_date (J+7 at 22h59)`() {
            val endDay = LocalDate.now().plusDays(1).plusDays(7)
            val firstColumn = lines.map { unquote(it[3]) }
            assertThat(firstColumn, everyItem(equalTo("${endDay}T21:59:00.000Z")))
        }

        private fun unquote(value: String) = value.removePrefix("\"").removeSuffix("\"")
    }
}
