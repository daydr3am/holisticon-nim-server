package dev.jadnb.nimgameserver.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Exception class for invalid inputs. If thrown in a Spring Context will cause
 *  * the web server to response with UNPROCESSABLE_ENTITY (422)
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
class InvalidInputException : RuntimeException {

    constructor(message: String): super(message)
    constructor(message: String, cause: Throwable): super(message, cause)
    constructor(cause: Throwable): super(cause)
}