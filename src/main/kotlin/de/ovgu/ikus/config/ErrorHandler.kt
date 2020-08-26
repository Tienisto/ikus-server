package de.ovgu.ikus.config

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.dto.Feedback
import de.ovgu.ikus.utils.toJSON
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Configuration
@Order(-2)
class GlobalErrorHandler : ErrorWebExceptionHandler {
    override fun handle(serverWebExchange: ServerWebExchange, throwable: Throwable): Mono<Void> {
        val (status, message) = when(throwable) {
            is ErrorCode -> Pair(throwable.code, throwable.info)
            is ResponseStatusException -> Pair(throwable.status.value(), throwable.reason)
            else -> {
                throwable.printStackTrace()
                Pair(500, throwable.message)
            }
        }

        serverWebExchange.response.statusCode = HttpStatus.valueOf(status)
        serverWebExchange.response.headers.contentType = MediaType.APPLICATION_JSON

        val responseJSON = Feedback(message).toJSON()
        val response = serverWebExchange.response.bufferFactory().wrap(responseJSON.toByteArray())

        return serverWebExchange.response.writeWith(Mono.just(response))
    }
}