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

    fun countCodeUsedAndValid(codeType: String, valid: Boolean) {
        val counter = codesCounters[codeType]!!.getOrPut(valid) {
            Counter.builder("submission.verify.code")
                .description("Submission code count per verify")
                .tag("code type", codeType)
                .tag("valid", "$valid")
                .register(meterRegistry)
        }
        counter.increment()
    }
}
