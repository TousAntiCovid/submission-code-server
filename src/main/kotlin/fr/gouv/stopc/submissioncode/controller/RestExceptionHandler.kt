package fr.gouv.stopc.submissioncode.controller

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import fr.gouv.stopc.submissioncode.api.model.ErrorDetails
import fr.gouv.stopc.submissioncode.api.model.ErrorResponse
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.OffsetDateTime
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolationException

@RestControllerAdvice
class RestExceptionHandler(private val servletRequest: HttpServletRequest) : ResponseEntityExceptionHandler() {

    private val log = getLogger(RestExceptionHandler::class.java)

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> = handleBindException(ex, headers, status, request)

    override fun handleBindException(
        ex: BindException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        return badRequestAndLogErrors(
            ex.fieldErrors.map {
                ErrorDetails(it.field, it.code ?: "", it.defaultMessage ?: "")
            } + ex.globalErrors.map {
                ErrorDetails("", it.code ?: "", it.defaultMessage ?: "")
            }
        )
    }

    @ExceptionHandler
    fun handle(ex: ConstraintViolationException): ResponseEntity<Any> {
        val errors = ex.constraintViolations.map {
            ErrorDetails(
                field = it.propertyPath.toString(),
                code = it.constraintDescriptor.annotation.annotationClass.java.simpleName,
                message = it.message
            )
        }
        return badRequestAndLogErrors(errors)
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val cause = ex.cause
        val error = if (cause is MismatchedInputException) {
            ErrorDetails(
                field = cause.path
                    .map { if (it.fieldName != null) ".${it.fieldName}" else "[${it.index}]" }
                    .joinToString("")
                    .removePrefix("."),
                code = "HttpMessageNotReadable",
                message = ex.rootCause?.message ?: ""
            )
        } else {
            ErrorDetails(
                field = "",
                code = ex::class.java.simpleName,
                message = ex.message ?: ""
            )
        }
        return badRequestAndLogErrors(listOf(error))
    }

    private fun badRequestAndLogErrors(errors: List<ErrorDetails>): ResponseEntity<Any> {
        val errorResponseBody = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = "Request body contains invalid attributes",
            timestamp = OffsetDateTime.now(),
            path = servletRequest.requestURI,
            errors = errors
        )
        errorResponseBody.errors?.forEach {
            log.info(
                "Validation error on {} {}: '{}' {}",
                servletRequest.method,
                servletRequest.requestURI,
                it.field,
                it.message
            )
        }
        return ResponseEntity(errorResponseBody, HttpStatus.BAD_REQUEST)
    }

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        super.handleExceptionInternal(ex, body, headers, status, request)
        val errorResponseBody = ErrorResponse(
            status = status.value(),
            error = status.reasonPhrase,
            message = ex.message ?: "Internal error",
            timestamp = OffsetDateTime.now(),
            path = servletRequest.requestURI
        )

        if (status.is4xxClientError) {
            log.info("Client side error on {} {}: {}", servletRequest.method, servletRequest.requestURI, ex.message)
        } else {
            log.error("An unexpected error occured", ex)
        }

        return ResponseEntity(errorResponseBody, status)
    }
}
