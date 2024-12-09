package de.ovgu.ikus.security

import de.ovgu.ikus.properties.RoutesProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter
import org.springframework.web.reactive.config.WebFluxConfigurer
import reactor.core.publisher.Mono


@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val routesProps: RoutesProperties,
    private val securityContextRepo: SecurityContextRepo,
) : WebFluxConfigurer {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .headers { headers ->
                headers.frameOptions { frameOptions ->
                    // e.g. embed pdf
                    frameOptions.mode(XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN)
                }
            }
            .requestCache { it.disable() }
            .authorizeExchange {
                if (!routesProps.dev) it.pathMatchers("/api/dev/**").denyAll() // dev API

                it.pathMatchers("/api/login", "/api/version", "/api/status", "/api/public/**").permitAll() // public API
                it.pathMatchers("/api/**").authenticated() // internal API
                it.pathMatchers("/**").permitAll() // static files
            }
            .securityContextRepository(securityContextRepo).exceptionHandling {
                it.authenticationEntryPoint { exchange, _ ->
                    exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                    Mono.empty()
                }
            }
            .build()
    }
}
