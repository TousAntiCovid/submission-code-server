package fr.gouv.stopc.submissioncode.service

import fr.gouv.stopc.submission.code.server.business.service.FileService
import fr.gouv.stopc.submission.code.server.business.service.schedule.DailyGenerateSchedule
import fr.gouv.stopc.submission.code.server.business.service.schedule.GenerationConfigProperties
import fr.gouv.stopc.submission.code.server.business.service.schedule.GenerationConfigProperties.GenerationConfig
import fr.gouv.stopc.submission.code.server.data.repository.SubmissionCodeRepository
import fr.gouv.stopc.submissioncode.csvOutputFileFormatSpecTest
import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.NamedContentFile
import fr.gouv.stopc.submissioncode.test.SftpManager
import fr.gouv.stopc.submissioncode.test.readTarGzEntries
import org.apache.commons.codec.digest.DigestUtils
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.matchesRegex
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@IntegrationTest
@TestInstance(PER_CLASS)
class LongCodeSchedulerTest(
    @Autowired submissionCodeRepository: SubmissionCodeRepository,
    @Autowired fileService: FileService
) {

    private val dailyGenerateSchedule: DailyGenerateSchedule

    private lateinit var generatedFiles: List<NamedContentFile>
    private lateinit var generatedTarGzArchives: List<NamedContentFile>
    private lateinit var generatedCsvFiles: List<NamedContentFile>

    init {
        val generationConfig = GenerationConfigProperties().apply {
            maxbatchsize = 40_000
            scheduling = listOf(
                GenerationConfig(OffsetDateTime.MIN, 100_000)
            )
        }
        dailyGenerateSchedule = DailyGenerateSchedule(submissionCodeRepository, fileService, generationConfig)
    }

    @BeforeAll
    fun execute_scheduler() {
        dailyGenerateSchedule.dailyProductionCodeScheduler()
        generatedFiles = SftpManager.listFiles()
        generatedTarGzArchives = generatedFiles
            .filter { it.name.endsWith(".tgz") }
        generatedCsvFiles = generatedTarGzArchives
            .flatMap { readTarGzEntries(it.content) }
    }

    @Test
    fun should_generate_6_files() {
        assertThat(generatedFiles, hasSize(6))
    }

    @Test
    fun should_generate_3_tar_gz_archives() {
        val generatedArchiveFiles = generatedFiles
            .filter { it.name.endsWith(".tgz") }
            .sortedBy { it.name }
        assertThat(generatedArchiveFiles, hasSize(3))
    }

    @Test
    fun should_generate_3_checksums() {
        val generatedChecksums = generatedFiles
            .filter { it.name.endsWith(".sha256") }
        assertThat(generatedChecksums, hasSize(3))
    }

    @Test
    fun should_generate_archives_with_valid_checksums() {
        val actualChecksums = generatedFiles
            .filter { it.name.endsWith(".sha256") }
            .map { Pair(it.name.replace("\\.sha256$".toRegex(), ".tgz"), it.content.decodeToString()) }

        val expectedChecksums = generatedFiles
            .filter { it.name.endsWith(".tgz") }
            .map { Pair(it.name, DigestUtils.sha256Hex(it.content)) }

        assertThat(actualChecksums)
            .containsExactlyInAnyOrderElementsOf(expectedChecksums)
    }

    @Nested
    @TestInstance(PER_CLASS)
    inner class GeneratedArchives {

        private fun allGeneratedArchives() = generatedTarGzArchives.map { arguments(it) }

        @ParameterizedTest
        @MethodSource("allGeneratedArchives")
        fun should_contain_11_files(generatedTarArchive: NamedContentFile) {
            val csvEntries = readTarGzEntries(generatedTarArchive.content)
            assertThat(csvEntries).hasSize(11)
        }

        @ParameterizedTest
        @MethodSource("allGeneratedArchives")
        fun should_contain_one_file_per_day(generatedTarArchive: NamedContentFile) {
            val dateFormatYYMMDD = DateTimeFormatter.ofPattern("yyMMdd")
            val today = LocalDate.now().format(dateFormatYYMMDD)
            val todayPlus1 = LocalDate.now().plusDays(1).format(dateFormatYYMMDD)
            val todayPlus2 = LocalDate.now().plusDays(2).format(dateFormatYYMMDD)
            val todayPlus3 = LocalDate.now().plusDays(3).format(dateFormatYYMMDD)
            val todayPlus4 = LocalDate.now().plusDays(4).format(dateFormatYYMMDD)
            val todayPlus5 = LocalDate.now().plusDays(5).format(dateFormatYYMMDD)
            val todayPlus6 = LocalDate.now().plusDays(6).format(dateFormatYYMMDD)
            val todayPlus7 = LocalDate.now().plusDays(7).format(dateFormatYYMMDD)
            val todayPlus8 = LocalDate.now().plusDays(8).format(dateFormatYYMMDD)
            val todayPlus9 = LocalDate.now().plusDays(9).format(dateFormatYYMMDD)
            val todayPlus10 = LocalDate.now().plusDays(10).format(dateFormatYYMMDD)
            val generatedCsvFileNames = readTarGzEntries(generatedTarArchive.content).map { it.name }
            assertThat(
                generatedCsvFileNames,
                containsInAnyOrder(
                    matchesRegex("^\\d+$today\\.csv$"),
                    matchesRegex("^\\d+$todayPlus1\\.csv$"),
                    matchesRegex("^\\d+$todayPlus2\\.csv$"),
                    matchesRegex("^\\d+$todayPlus3\\.csv$"),
                    matchesRegex("^\\d+$todayPlus4\\.csv$"),
                    matchesRegex("^\\d+$todayPlus5\\.csv$"),
                    matchesRegex("^\\d+$todayPlus6\\.csv$"),
                    matchesRegex("^\\d+$todayPlus7\\.csv$"),
                    matchesRegex("^\\d+$todayPlus8\\.csv$"),
                    matchesRegex("^\\d+$todayPlus9\\.csv$"),
                    matchesRegex("^\\d+$todayPlus10\\.csv$")
                )
            )
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    inner class GeneratedCsvFiles {

        fun csv_files_from_all_archives_except_last() = generatedTarGzArchives.dropLast(1)
            .flatMap { readTarGzEntries(it.content) }

        fun csv_files_from_last_archive() = readTarGzEntries(generatedTarGzArchives.last().content)

        @ParameterizedTest
        @MethodSource("csv_files_from_all_archives_except_last")
        fun should_contain_1_header_line_plus_40k_value_lines(csvFile: NamedContentFile) {
            val csvRawContent = csvFile.content
                .decodeToString()
                .removeSuffix("\n")
            assertThat(csvRawContent.lines())
                .hasSize(40_000 + 1)
        }

        @ParameterizedTest
        @MethodSource("csv_files_from_last_archive")
        fun should_contain_1_header_line_plus_20k_value_lines(csvFile: NamedContentFile) {
            val csvRawContent = csvFile.content
                .decodeToString()
                .removeSuffix("\n")
            assertThat(csvRawContent.lines())
                .hasSize(20_000 + 1)
        }

        @TestFactory
        fun should_be_compliant_to_the_spec() = generatedCsvFiles
            .map { dynamicContainer("csv file ${it.name}", csvOutputFileFormatSpecTest(it)) }
    }
}
