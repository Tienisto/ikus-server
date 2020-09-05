package de.ovgu.ikus.security

import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.config.WebFluxConfigurer
import reactor.core.publisher.Mono


@EnableWebFluxSecurity
class SecurityConfig (private val securityContextRepo: SecurityContextRepo) : WebFluxConfigurer {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
                .csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()
                .authorizeExchange()
                .pathMatchers("/api/login", "/api/version", "/api/status", "/api/aggregations/**").permitAll()
                .pathMatchers("/api/**").authenticated()
                .pathMatchers("/**").permitAll()
                .and()
                .securityContextRepository(securityContextRepo)
                .exceptionHandling().authenticationEntryPoint { exchange, _ ->
                    exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                    Mono.empty()
                }
                .and()
                .build()
    }
}