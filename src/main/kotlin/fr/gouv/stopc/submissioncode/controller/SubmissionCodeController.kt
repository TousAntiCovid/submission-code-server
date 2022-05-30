package fr.gouv.stopc.submissioncode.controller

import fr.gouv.stopc.submissioncode.api.SubmissionCodeApi
import fr.gouv.stopc.submissioncode.api.model.CodeType
import fr.gouv.stopc.submissioncode.api.model.CodeType.short
import fr.gouv.stopc.submissioncode.api.model.CodeType.test
import fr.gouv.stopc.submissioncode.api.model.SubmissionCodeGenerationResponse
import fr.gouv.stopc.submissioncode.api.model.SubmissionCodeValidationResponse
import fr.gouv.stopc.submissioncode.service.SubmissionCodeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZoneOffset.UTC

@RestController
@RequestMapping("/api/v1")
class SubmissionCodeController(private val submissionCodeService: SubmissionCodeService) : SubmissionCodeApi {

    override fun generate(codeType: CodeType): ResponseEntity<SubmissionCodeGenerationResponse> {
        val generatedCode = when (codeType) {
            short -> submissionCodeService.generateShortCode()
            test -> submissionCodeService.generateTestCode()
        }
        return ResponseEntity.ok(
            SubmissionCodeGenerationResponse(
                code = generatedCode.code,
                dateGenerate = generatedCode.dateGeneration.atOffset(UTC),
                validFrom = generatedCode.dateAvailable.atOffset(UTC),
                validUntil = generatedCode.dateEndValidity.atOffset(UTC)
            )
        )
    }

    override fun verify(code: String, deprecatedParameter: Int?): ResponseEntity<SubmissionCodeValidationResponse> {
        return ResponseEntity.ok(
            SubmissionCodeValidationResponse(submissionCodeService.validateAndUse(code))
        )
    }
}
