package fr.gouv.stopc.submissioncode.test

import fr.gouv.stopc.submissioncode.test.LongCodesCsvFile.LongCodeLine
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream

data class NamedContentFile(val name: String, val content: ByteArray) {
    override fun toString(): String {
        return "NamedContentFile(name=$name, content=${content.size} bytes)"
    }
}

fun readTarGzEntries(tarGzArchive: ByteArray): List<NamedContentFile> {
    return TarArchiveInputStream(GZIPInputStream(ByteArrayInputStream(tarGzArchive)))
        .use { tar ->
            generateSequence { tar.nextTarEntry }
                .map { entry -> NamedContentFile(entry.name, tar.readAllBytes()) }
                .toList()
        }
}

class LongCodesCsvFile(csvFileContent: String) : Iterable<LongCodeLine> {
    val header: String
    private val codes: List<LongCodeLine>

    init {
        header = csvFileContent.lines().first()
        codes = csvFileContent
            .removeSuffix("\n")
            .lines()
            .drop(1)
            .map { it.removePrefix("\"").removeSuffix("\"").split("\",\"") }
            .map { LongCodeLine(link = it[0], code = it[1], start = it[2], end = it[3]) }
    }

    data class LongCodeLine(
        val link: String,
        val code: String,
        val start: String,
        val end: String
    )

    override fun iterator() = codes.iterator()
}
