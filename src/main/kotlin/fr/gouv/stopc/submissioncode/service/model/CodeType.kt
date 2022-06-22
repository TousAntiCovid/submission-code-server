package fr.gouv.stopc.submissioncode.service.model

import fr.gouv.stopc.submissioncode.repository.model.SubmissionCode
import kotlin.text.RegexOption.IGNORE_CASE

enum class CodeType(
    private val pattern: Regex,
    val databaseRepresentation: SubmissionCode.Type?
) {
    LONG("[a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8}".toRegex(IGNORE_CASE), SubmissionCode.Type.LONG),
    SHORT("[a-z0-9]{6}".toRegex(IGNORE_CASE), SubmissionCode.Type.SHORT),
    TEST("[a-z0-9]{12}".toRegex(IGNORE_CASE), SubmissionCode.Type.TEST),
    JWT("^[^.]+\\.[^.]+\\.[^.]+$".toRegex(), null);

    companion object {

        fun ofCode(code: String) = values().find { code.matches(it.pattern) }
    }
}
