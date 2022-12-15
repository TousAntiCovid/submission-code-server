package fr.gouv.stopc.submissioncode

import fr.gouv.stopc.submissioncode.configuration.SubmissionProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(SubmissionProperties::class)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
