package fr.gouv.stopc.submissioncode.controller

import fr.gouv.stopc.submissioncode.api.SubmissionCodeApi
import fr.gouv.stopc.submissioncode.api.model.SubmissionCodeValidationResponse
import fr.gouv.stopc.submissioncode.service.SubmissionCodeJWTService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SubmissionCodeJWTController(private val submissionCodeJWTService: SubmissionCodeJWTService) : SubmissionCodeApi {

    @GetMapping("/api/v1/verifyJwt")
    override fun verifyJwt(jwt: String): ResponseEntity<SubmissionCodeValidationResponse> {
        return ResponseEntity.ok(
            SubmissionCodeValidationResponse(submissionCodeJWTService.validateJwt(jwt))
        )
    }
}
