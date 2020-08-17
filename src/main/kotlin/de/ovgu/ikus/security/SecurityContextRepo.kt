package de.ovgu.ikus.security

import kotlinx.coroutines.reactor.mono
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class SecurityContextRepo (
        private val jwtService: JwtService
) : ServerSecurityContextRepository {

    override fun save(exchange: ServerWebExchange?, context: SecurityContext?): Mono<Void> {
        return Mono.empty()
    }

    override fun load(exchange: ServerWebExchange): Mono<SecurityContext> {

        val token = exchange.request.headers["Authorization"]?.firstOrNull()?.replace("Bearer ", "")
        if(token != null) {
            // TODO: this mono is called twice
            return mono {
                val auth = jwtService.getAuthentication(token)
                if (auth != null)
                    SecurityContextImpl(auth)
                else
                    null
            }
        } else {
            return Mono.empty()
        }
    }

}