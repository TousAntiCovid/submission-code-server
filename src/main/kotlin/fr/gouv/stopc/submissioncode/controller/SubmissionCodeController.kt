package fr.gouv.stopc.submissioncode.controller

import fr.gouv.stopc.submissioncode.api.SubmissionCodeApi
import fr.gouv.stopc.submissioncode.api.model.SubmissionCodeValidationResponse
import fr.gouv.stopc.submissioncode.service.SubmissionCodeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class SubmissionCodeController(
    private val submissionCodeService: SubmissionCodeService
) : SubmissionCodeApi {

    override fun verify(code: String, deprecatedCodeType: Int?): ResponseEntity<SubmissionCodeValidationResponse> {
        return ResponseEntity.ok(
            SubmissionCodeValidationResponse(submissionCodeService.validateJwt(code))
        )
    }
}
