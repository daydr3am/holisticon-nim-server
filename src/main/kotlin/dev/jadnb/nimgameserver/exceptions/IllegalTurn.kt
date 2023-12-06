package dev.jadnb.nimgameserver.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Exception class for all illegal moves. If thrown in a Spring Context will cause
 * the web server to response with FORBIDDEN (403)
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
class IllegalTurn : RuntimeException {
    constructor(message: String) : super(message) {
    }

    constructor(message: String, throwable: Throwable) : super(message, throwable)
}