package fr.gouv.stopc.submissioncode.test

import io.restassured.RestAssured
import io.restassured.specification.RequestSpecification
import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener

class RestAssuredManager : TestExecutionListener {

    override fun beforeTestClass(testContext: TestContext) = beforeTestMethod(testContext)

    override fun beforeTestMethod(testContext: TestContext) {
        RestAssured.port = testContext.applicationContext
            .environment
            .getRequiredProperty("local.server.port", Int::class.java)
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }
}

fun When() = RestAssured.`when`()

fun RequestSpecification.When() = this.`when`()
