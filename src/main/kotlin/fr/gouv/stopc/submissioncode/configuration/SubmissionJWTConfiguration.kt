package fr.gouv.stopc.submissioncode.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "submission.code.server")
class SubmissionJWTConfiguration {

    val mapOfKidAndPublicKey: MutableMap<String, String> = mutableMapOf()
}
