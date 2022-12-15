package fr.gouv.stopc.submissioncode.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service

@Service
class MetricsService(meterRegistry: MeterRegistry) {

    private val codesCounters =
        mapOf(
            true to Counter.builder("submission.verify.code")
                .description("Submission codes verified")
                .tag("valid", "true")
                .register(meterRegistry),
            false to Counter.builder("submission.verify.code")
                .description("Submission codes rejected")
                .tag("valid", "false")
                .register(meterRegistry)
        )

    fun countCodeUsed(valid: Boolean) = codesCounters[valid]!!.increment()
}
