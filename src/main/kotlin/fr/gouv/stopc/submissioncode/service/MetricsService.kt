package fr.gouv.stopc.submissioncode.service

import fr.gouv.stopc.submissioncode.service.model.CodeType
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service

@Service
class MetricsService(private val meterRegistry: MeterRegistry) {

    private val codesCounters = CodeType.values().associateWith { codeType ->
        mapOf(
            true to Counter.builder("submission.verify.code")
                .description("Submission codes verified")
                .tag("code type", codeType.name)
                .tag("valid", "true")
                .register(meterRegistry),
            false to Counter.builder("submission.verify.code")
                .description("Submission codes rejected")
                .tag("code type", codeType.name)
                .tag("valid", "false")
                .register(meterRegistry)
        )
    }

    fun countCodeUsed(codeType: CodeType, valid: Boolean) = codesCounters[codeType]!![valid]!!.increment()
}
