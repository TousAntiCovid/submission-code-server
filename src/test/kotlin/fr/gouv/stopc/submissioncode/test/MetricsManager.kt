package fr.gouv.stopc.submissioncode.test

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.getBean
import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener

class MetricsManager : TestExecutionListener {

    companion object {

        private lateinit var meterRegistry: MeterRegistry

        fun getCodeVerificationCount(name: String, vararg tags: String): Int = try {
            meterRegistry
                .get(name)
                .tags(*tags)
                .counter()
                .count().toInt()
        } catch (e: NullPointerException) {
            0
        }
    }

    override fun beforeTestMethod(testContext: TestContext) {
        meterRegistry = testContext.applicationContext.getBean(MeterRegistry::class)
    }
}
