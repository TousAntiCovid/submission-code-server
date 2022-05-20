package fr.gouv.stopc.submissioncode.service

import org.apache.commons.text.RandomStringGenerator
import org.springframework.stereotype.Component

@Component
class RandomGenerator {

    private val upperCaseString = RandomStringGenerator.Builder()
        .selectFrom(*"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray())
        .build()

    fun upperCaseString(lenght: Int): String = upperCaseString.generate(lenght)
}
