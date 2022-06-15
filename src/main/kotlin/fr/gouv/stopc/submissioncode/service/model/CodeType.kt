package fr.gouv.stopc.submissioncode.service.model

enum class CodeType(private val size: Int) {
    LONG(36),
    SHORT(6),
    TEST(12),
    JWT(-1);

    companion object {
        fun ofCode(code: String) = values().find { code.length == it.size } ?: JWT
    }
}
