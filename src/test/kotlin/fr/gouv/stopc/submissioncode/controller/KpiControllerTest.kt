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
            .body("[0].nbShortCodesUsed", equalTo(0))
            .body("[0].nbLongCodesUsed", equalTo(0))
            .body("[0].nbTestCodesUsed", equalTo(0))
            .body("[0].nbJwtUsed", equalTo(0))
            .body("[0].nbLongExpiredCodes", equalTo(0))
            .body("[0].nbShortExpiredCodes", equalTo(0))
            .body("[0].nbTestExpiredCodes", equalTo(0))
            .body("[0].nbShortCodesGenerated", equalTo(0))
            .body("[0].nbTestCodesGenerated", equalTo(0))
            .body("size()", equalTo(1))
    }

    @Test
    fun can_fetch_kpis(@Autowired jdbcTemplate: JdbcTemplate) {
        // ok 👌 this is ugly,
        // focus on describing the actual v1 behavior to have TNR and be able to rewrite the whole application
        jdbcTemplate.execute(
            """
                insert into submission_code(type_code, code, date_generation, date_available, date_end_validity, date_use, used) values
                -- codes statistics for october 31:
                ('2',                               'AAAAA1', '2021-10-15', '2021-10-15', '2021-11-14', '2021-10-31',  true), -- 2 used short codes
                ('2',                               'AAAAA2', '2021-10-15', '2021-10-15', '2021-11-14', '2021-10-31',  true),
                ('2',                               'AAAAA3', '2021-10-15', '2021-10-15', '2021-10-31',         null, false), -- 3 expired short codes
                ('2',                               'AAAAA4', '2021-10-15', '2021-10-15', '2021-10-31',         null, false),
                ('2',                               'AAAAA5', '2021-10-15', '2021-10-15', '2021-10-31',         null, false),
                ('2',                               'AAAAA6', '2021-10-31', '2021-10-31', '2021-11-14',         null, false), -- 4 generated short codes
                ('2',                               'AAAAA7', '2021-10-31', '2021-10-31', '2021-11-14',         null, false),
                ('2',                               'AAAAA8', '2021-10-31', '2021-10-31', '2021-11-14',         null, false),
                ('2',                               'AAAAA9', '2021-10-31', '2021-10-31', '2021-11-14',         null, false),
                ('1', '00000000-0000-0000-0000-000000000001', '2021-10-15', '2021-10-15', '2021-11-14', '2021-10-31',  true), -- 5 used long codes
                ('1', '00000000-0000-0000-0000-000000000002', '2021-10-15', '2021-10-15', '2021-11-14', '2021-10-31',  true),
                ('1', '00000000-0000-0000-0000-000000000003', '2021-10-15', '2021-10-15', '2021-11-14', '2021-10-31',  true),
                ('1', '00000000-0000-0000-0000-000000000004', '2021-10-15', '2021-10-15', '2021-11-14', '2021-10-31',  true),
                ('1', '00000000-0000-0000-0000-000000000005', '2021-10-15', '2021-10-15', '2021-11-14', '2021-10-31',  true),
                ('1', '00000000-0000-0000-0000-000000000006', '2021-10-15', '2021-10-15', '2021-10-31',         null, false), -- 6 expired long codes
                ('1', '00000000-0000-0000-0000-000000000007', '2021-10-15', '2021-10-15', '2021-10-31',         null, false),
                ('1', '00000000-0000-0000-0000-000000000008', '2021-10-15', '2021-10-15', '2021-10-31',         null, false),
                ('1', '00000000-0000-0000-0000-000000000009', '2021-10-15', '2021-10-15', '2021-10-31',         null, false),
                ('1', '00000000-0000-0000-0000-000000000010', '2021-10-15', '2021-10-15', '2021-10-31',         null, false),
                ('1', '00000000-0000-0000-0000-000000000011', '2021-10-15', '2021-10-15', '2021-10-31',         null, false),
                ('3',                               'AAAAAAAAAAA1', '2021-10-15', '2021-10-15', '2021-11-14', '2021-10-31',  true), -- 3 used test codes
                ('3',                               'AAAAAAAAAAA2', '2021-10-15', '2021-10-15', '2021-11-14', '2021-10-31',  true),
                ('3',                               'AAAAAAAAAAA3', '2021-10-15', '2021-10-15', '2021-11-14', '2021-10-31',  true),
                ('3',                               'AAAAAAAAAAA4', '2021-10-15', '2021-10-15', '2021-10-31',         null, false), -- 4 expired test codes
                ('3',                               'AAAAAAAAAAA5', '2021-10-15', '2021-10-15', '2021-10-31',         null, false),
                ('3',                               'AAAAAAAAAAA6', '2021-10-15', '2021-10-15', '2021-10-31',         null, false),
                ('3',                               'AAAAAAAAAAA7', '2021-10-15', '2021-10-15', '2021-10-31',         null, false),
                ('3',                               'AAAAAAAAAAA8', '2021-10-31', '2021-10-31', '2021-11-14',         null, false), -- 5 generated test codes
                ('3',                               'AAAAAAAAAAA9', '2021-10-31', '2021-10-31', '2021-11-14',         null, false),
                ('3',                               'AAAAAAAAAA10', '2021-10-31', '2021-10-31', '2021-11-14',         null, false),
                ('3',                               'AAAAAAAAAA11', '2021-10-31', '2021-10-31', '2021-11-14',         null, false),
                ('3',                               'AAAAAAAAAA12', '2021-10-31', '2021-10-31', '2021-11-14',         null, false),
                -- codes statistics for november 1:
                ('2',                               'BBBBB1', '2021-10-15', '2021-10-15', '2021-11-14', '2021-11-01',  true), -- 1 used short codes
                ('2',                               'BBBBB2', '2021-10-15', '2021-10-15', '2021-11-01',         null, false), -- 2 expired short codes
                ('2',                               'BBBBB3', '2021-10-15', '2021-10-15', '2021-11-01',         null, false),
                ('2',                               'BBBBB4', '2021-11-01', '2021-11-01', '2021-11-14',         null, false), -- 3 generated short codes
                ('2',                               'BBBBB5', '2021-11-01', '2021-11-01', '2021-11-14',         null, false),
                ('2',                               'BBBBB6', '2021-11-01', '2021-11-01', '2021-11-14',         null, false),
                ('1', '00000000-0000-0000-0001-000000000001', '2021-10-15', '2021-10-15', '2021-11-14', '2021-11-01',  true), -- 4 used long codes
                ('1', '00000000-0000-0000-0001-000000000002', '2021-10-15', '2021-10-15', '2021-11-14', '2021-11-01',  true),
                ('1', '00000000-0000-0000-0001-000000000003', '2021-10-15', '2021-10-15', '2021-11-14', '2021-11-01',  true),
                ('1', '00000000-0000-0000-0001-000000000004', '2021-10-15', '2021-10-15', '2021-11-14', '2021-11-01',  true),
                ('1', '00000000-0000-0000-0001-000000000006', '2021-10-15', '2021-10-15', '2021-11-01',         null, false), -- 5 expired long codes
                ('1', '00000000-0000-0000-0001-000000000007', '2021-10-15', '2021-10-15', '2021-11-01',         null, false),
                ('1', '00000000-0000-0000-0001-000000000008', '2021-10-15', '2021-10-15', '2021-11-01',         null, false),
                ('1', '00000000-0000-0000-0001-000000000009', '2021-10-15', '2021-10-15', '2021-11-01',         null, false),
                ('1', '00000000-0000-0000-0001-000000000010', '2021-10-15', '2021-10-15', '2021-11-01',         null, false),
                ('3',                               'BBBBBBBBBBB1', '2021-10-15', '2021-10-15', '2021-11-14', '2021-11-01',  true), -- 2 used test codes
                ('3',                               'BBBBBBBBBBB2', '2021-10-15', '2021-10-15', '2021-11-14', '2021-11-01',  true),
                ('3',                               'BBBBBBBBBBB3', '2021-10-15', '2021-10-15', '2021-11-01',         null, false), -- 3 expired test codes
                ('3',                               'BBBBBBBBBBB4', '2021-10-15', '2021-10-15', '2021-11-01',         null, false),
                ('3',                               'BBBBBBBBBBB5', '2021-10-15', '2021-10-15', '2021-11-01',         null, false),
                ('3',                               'BBBBBBBBBBB6', '2021-11-01', '2021-11-01', '2021-11-14',         null, false), -- 4 generated test codes
                ('3',                               'BBBBBBBBBBB7', '2021-11-01', '2021-11-01', '2021-11-14',         null, false),
                ('3',                               'BBBBBBBBBBB8', '2021-11-01', '2021-11-01', '2021-11-14',         null, false),
                ('3',                               'BBBBBBBBBBB9', '2021-11-01', '2021-11-01', '2021-11-14',         null, false)
            """.trimIndent()
        )

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
            .body("[0].nbShortCodesUsed", equalTo(2))
            .body("[0].nbShortExpiredCodes", equalTo(3))
            .body("[0].nbShortCodesGenerated", equalTo(4))
            .body("[0].nbLongCodesUsed", equalTo(5))
            .body("[0].nbLongExpiredCodes", equalTo(6))
            .body("[0].nbTestCodesUsed", equalTo(3))
            .body("[0].nbTestExpiredCodes", equalTo(4))
            .body("[0].nbTestCodesGenerated", equalTo(5))
            .body("[0].nbJwtUsed", equalTo(1))
            .body("[1].date", equalTo("2021-11-01"))
            .body("[1].nbShortCodesUsed", equalTo(1))
            .body("[1].nbShortExpiredCodes", equalTo(2))
            .body("[1].nbShortCodesGenerated", equalTo(3))
            .body("[1].nbLongCodesUsed", equalTo(4))
            .body("[1].nbLongExpiredCodes", equalTo(5))
            .body("[1].nbTestCodesUsed", equalTo(2))
            .body("[1].nbTestExpiredCodes", equalTo(3))
            .body("[1].nbTestCodesGenerated", equalTo(4))
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
