package dev.jadnb.nimgameserver.exceptions

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Exception class for all not found resources. If thrown in a Spring Context will cause
 *  the web server to response with NOT FOUND (404)
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException(msg: String) : RuntimeException(msg)