package fr.gouv.stopc.submissioncode

import fr.gouv.stopc.submissioncode.test.NamedContentFile
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.everyItem
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.matchesPattern
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.startsWith
import org.hamcrest.Matchers.stringContainsInOrder
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.TimeZone

/**
 * Returns a list of DynamicTest to verify the given file is a well formed CSV file containing long codes.
 *
 * @param csvFile the file to verify
 * @param startDateZoneId a ZoneId used for validity start date (defaults to Europe/Paris, but REST API endpoint generates files with an UTC start date)
 */
fun csvOutputFileFormatSpecTest(
    csvFile: NamedContentFile,
    startDateZoneId: ZoneId = TimeZone.getTimeZone("Europe/Paris").toZoneId()
): List<DynamicTest> {
    val csvFileRawContent = csvFile.content.decodeToString()
    val lines = csvFileRawContent
        .removeSuffix("\n")
        .lines()
        .drop(1)
        .map { it.split(",") }
    return listOf(
        dynamicTest("header has 4 unquoted columns") {
            assertThat(
                csvFileRawContent,
                startsWith("code_pour_qr, code_brut, validite_debut, validite_fin\n")
            )
        },
        dynamicTest("ends with an empty line") {
            assertThat(csvFileRawContent.lines().last(), equalTo(""))
        },
        dynamicTest("all lines ends with lf") {
            assertThat(csvFileRawContent, not(stringContainsInOrder("\r\n")))
        },
        dynamicTest("lines have 4 values delimited by comma") {
            assertThat(lines, everyItem(hasSize(4)))
        },
        dynamicTest("values are surrounded with double quotes") {
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
        },
        dynamicTest("column 1 contains an url") {
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
        },
        dynamicTest("column 2 contains a code") {
            val firstColumn = lines.map { unquote(it[1]) }
            assertThat(firstColumn, everyItem(matchesPattern("[a-z0-9-]{36}")))
        },
        dynamicTest("column 3 contains the validity start date, it's a date expressed at zone UTC") {
            val startDate = extractDateFromFileName(csvFile)
            val expectedStartDate = startDate
                .atStartOfDay(startDateZoneId)
                .toInstant()
                .toString().replace(":00Z", ":00.000Z")
            val firstColumn = lines.map { unquote(it[2]) }
            assertThat(firstColumn, everyItem(equalTo(expectedStartDate)))
        },
        dynamicTest("column 4 contains the validity end date (J+7 expressed at zone Europe/Paris)") {
            val startDate = extractDateFromFileName(csvFile)
            val endDateTime = startDate.plusDays(7)
                .atStartOfDay(TimeZone.getTimeZone("Europe/Paris").toZoneId())
                .withHour(23).withMinute(59)
                .toInstant()
                .toString()
                .replace("Z", ".000Z")
            val firstColumn = lines.map { unquote(it[3]) }
            assertThat(firstColumn, everyItem(equalTo(endDateTime)))
        }

    )
}

private fun extractDateFromFileName(csvFile: NamedContentFile): LocalDate {
    val dateString = csvFile.name.replace("\\d+(\\d{6})\\.csv".toRegex(), "$1")
    val datePattern = DateTimeFormatter.ofPattern("yyMMdd")
    return LocalDate.parse(dateString, datePattern)
}

private fun unquote(value: String) = value.removePrefix("\"").removeSuffix("\"")
