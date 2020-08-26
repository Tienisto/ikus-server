package de.ovgu.ikus.dto

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ErrorCode(val code: Int, val info: String) : ResponseStatusException(when(code) {
    400 -> HttpStatus.BAD_REQUEST
    403 -> HttpStatus.FORBIDDEN
    404 -> HttpStatus.NOT_FOUND
    409 -> HttpStatus.CONFLICT
    410 -> HttpStatus.GONE
    412 -> HttpStatus.PRECONDITION_FAILED
    417 -> HttpStatus.EXPECTATION_FAILED
    418 -> HttpStatus.I_AM_A_TEAPOT
    423 -> HttpStatus.LOCKED
    500 -> HttpStatus.INTERNAL_SERVER_ERROR
    else -> HttpStatus.INTERNAL_SERVER_ERROR
}, info)