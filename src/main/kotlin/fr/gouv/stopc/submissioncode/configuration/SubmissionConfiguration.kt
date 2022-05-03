package fr.gouv.stopc.submissioncode.configuration

import org.apache.commons.text.RandomStringGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SubmissionConfiguration {

    @Bean
    fun randomUppercaseString(): RandomStringGenerator = RandomStringGenerator.Builder()
        .selectFrom(*"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray())
        .build()
}
