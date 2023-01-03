package fr.gouv.stopc.submissioncode.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.time.Duration

@ConstructorBinding
@ConfigurationProperties(prefix = "submission")
data class SubmissionProperties(

    val longCodeLifetime: Duration,

    val shortCodeLifetime: Duration,

    val testCodeLifetime: Duration,

    val jwtCodeLifetime: Duration,

    val jwtPublicKeys: Map<String, String>
)
