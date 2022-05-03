package fr.gouv.stopc.submissioncode

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class SubmissionCodeServerApplication

fun main(args: Array<String>) {
    runApplication<SubmissionCodeServerApplication>(*args)
}
