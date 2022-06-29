package fr.gouv.stopc.submissioncode.test

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.MeterRegistry
import org.assertj.core.api.AbstractDoubleAssert
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.getBean
import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener

class MetricsManager : TestExecutionListener {

    companion object {

        private lateinit var meterRegistry: MeterRegistry

        private lateinit var countersSnapshot: Map<Meter.Id, Double>

        fun assertThatMetricCounterIncrement(name: String, vararg tags: String): AbstractDoubleAssert<*> {
            val counter = meterRegistry.counter(name, *tags)
            val previousValue = countersSnapshot[counter.id] ?: 0.0
            return assertThat(counter.count() - previousValue)
        }
    }

    override fun beforeTestMethod(testContext: TestContext) {
        meterRegistry = testContext.applicationContext.getBean(MeterRegistry::class)

        countersSnapshot = meterRegistry.meters
            .filterIsInstance<Counter>()
            .associate { it.id to it.count() }
    }
}
