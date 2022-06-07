package fr.gouv.stopc.submissioncode.configuration

import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.ECDSAVerifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.security.KeyFactory
import java.security.interfaces.ECPublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

@Configuration
class SubmissionConfiguration(private val submissionProperties: SubmissionProperties) {

    @Bean
    fun jwtSignatureVerifiers(): Map<String, JWSVerifier> = submissionProperties.jwtPublicKeys.mapValues {
        val decodedPublicKey = Base64.getDecoder().decode(it.value)
        val publicKey = KeyFactory.getInstance("EC")
            .generatePublic(X509EncodedKeySpec(decodedPublicKey))
        ECDSAVerifier(publicKey as ECPublicKey)
    }
}
