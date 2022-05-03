package fr.gouv.stopc.submissioncode.controller

import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(NOT_FOUND)
class HttpNotFoundException() : RuntimeException()
