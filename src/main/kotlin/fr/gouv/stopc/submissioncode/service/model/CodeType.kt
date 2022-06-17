package fr.gouv.stopc.submissioncode.service.model

enum class CodeType(private val size: Int, private val pattern: String) {
    LONG(36, "[a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8}"),
    SHORT(6, "[a-zA-Z0-9]{6}"),
    TEST(12, "[a-zA-Z0-9]{12}"),
    JWT(-1, "^[^.]+\\.[^.]+\\.[^.]+$");

    companion object {

        fun ofCode(code: String) = values().find { code.length == it.size } ?: JWT

        fun matchPattern(code: String): Boolean = code.matches(ofCode(code).pattern.toRegex())
    }
}
