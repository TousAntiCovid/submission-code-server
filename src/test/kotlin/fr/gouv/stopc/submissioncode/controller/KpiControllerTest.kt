package fr.gouv.stopc.submissioncode.controller

import fr.gouv.stopc.submissioncode.test.IntegrationTest
import fr.gouv.stopc.submissioncode.test.When
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.OK
import org.springframework.jdbc.core.JdbcTemplate

@IntegrationTest
class KpiControllerTest {

    @Test
    fun can_fetch_empty_kpis() {
        When()
            .get("/internal/api/v1/kpi?fromDate={from}&toDate={to}", "2021-10-19", "2021-10-19")

            .then()
            .statusCode(OK.value())
            .body("[0].date", equalTo("2021-10-19"))
            .body("[0].nbJwtUsed", equalTo(0))
            .body("size()", equalTo(1))
    }

    @Test
    fun can_fetch_kpis(@Autowired jdbcTemplate: JdbcTemplate) {
        // ok 👌 this is ugly,
        // focus on describing the actual v1 behavior to have TNR and be able to rewrite the whole application
        jdbcTemplate.execute(
            """
            insert into jwt_used(jti, date_use) values
            -- jwt statistics for october 31:
            ('jti1','2021-10-31'), -- 1 used jwt
            -- jwt statistics for november 1:
            ('jti2','2021-11-01'), -- 2 used jwt
            ('jti3','2021-11-01')
            """.trimIndent()
        )

        When()
            .get("/internal/api/v1/kpi?fromDate={from}&toDate={to}", "2021-10-31", "2021-11-01")

            .then()
            .statusCode(OK.value())
            .body("[0].date", equalTo("2021-10-31"))
            .body("[0].nbJwtUsed", equalTo(1))
            .body("[1].date", equalTo("2021-11-01"))
            .body("[1].nbJwtUsed", equalTo(2))
            .body("size()", equalTo(2))
    }

    @Test
    fun cant_fetch_more_than_100_days_of_kpis() {
        When()
            .get("/internal/api/v1/kpi?fromDate={from}&toDate={to}", "2021-01-01", "2021-04-12")

            .then()
            .statusCode(BAD_REQUEST.value())
            .body("status", equalTo(400))
            .body("error", equalTo("Bad Request"))
            .body("message", equalTo("Request body contains invalid attributes"))
            .body("path", equalTo("/internal/api/v1/kpi"))
            .body("errors[0].field", equalTo(""))
            .body("errors[0].code", equalTo("PERIOD_TOO_LARGE"))
            .body("errors[0].message", equalTo("The period between 'fromDate' and 'toDate' must be less than 100 days"))
            .body("errors.size()", equalTo(1))
    }

    @Test
    fun cant_fetch_kpis_when_end_is_before_start() {
        When()
            .get("/internal/api/v1/kpi?fromDate={from}&toDate={to}", "2021-11-01", "2021-10-31")

            .then()
            .statusCode(BAD_REQUEST.value())
            .body("status", equalTo(400))
            .body("error", equalTo("Bad Request"))
            .body("message", equalTo("Request body contains invalid attributes"))
            .body("path", equalTo("/internal/api/v1/kpi"))
            .body("errors[0].field", equalTo(""))
            .body("errors[0].code", equalTo("INCONSISTENT_BOUNDS"))
            .body("errors[0].message", equalTo("The 'fromDate' should be before 'toDate'"))
            .body("errors.size()", equalTo(1))
    }
}
