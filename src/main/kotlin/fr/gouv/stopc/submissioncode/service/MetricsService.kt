package fr.gouv.stopc.submissioncode.service

import fr.gouv.stopc.submissioncode.repository.model.SubmissionCode.Type
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service

@Service
class MetricsService(
    private val meterRegistry: MeterRegistry
) {

    private val codesCounters: Map<String, MutableMap<Boolean, Counter>> =
        Type.values().map(Type::name).associateWith { mutableMapOf() }

    private val jwtCounters: MutableMap<Boolean, Counter> = mutableMapOf()

    fun countCodeUsed(codeType: String, valid: Boolean) {
        val counter = codesCounters[codeType]!!.getOrPut(valid) {
            Counter.builder("submission.verify.code")
                .description("Submission code count per verify")
                .tag("code type", codeType)
                .tag("valid", "$valid")
                .register(meterRegistry)
        }
        counter.increment()
    }

    fun countJwtUsed(valid: Boolean) {
        val counter = jwtCounters.getOrPut(valid) {
            Counter.builder("submission.verify.jwt")
                .description("Submission jwt count per verify")
                .tag("valid", "$valid")
                .register(meterRegistry)
        }

        counter.increment()
    }
}
