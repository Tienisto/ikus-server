package de.ovgu.ikus.security

import de.ovgu.ikus.properties.RoutesProperties
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter
import org.springframework.web.reactive.config.WebFluxConfigurer
import reactor.core.publisher.Mono


@EnableWebFluxSecurity
class SecurityConfig (
        private val routesProps: RoutesProperties,
        private val securityContextRepo: SecurityContextRepo
) : WebFluxConfigurer {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
                .csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()
                .headers().frameOptions().mode(XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN) // e.g. embed pdf
                .cache().disable()
                .and()
                .authorizeExchange()
                .apply {
                    if (!routesProps.dev)
                        pathMatchers("/api/dev/**").denyAll() // dev API
                }
                .pathMatchers("/api/login", "/api/version", "/api/status", "/api/public/**").permitAll() // public API
                .pathMatchers("/api/**").authenticated() // internal API
                .pathMatchers("/**").permitAll() // static files
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