package fr.gouv.stopc.submissioncode

import fr.gouv.stopc.submissioncode.configuration.SubmissionProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@EnableConfigurationProperties(SubmissionProperties::class)
@EnableRetry
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
