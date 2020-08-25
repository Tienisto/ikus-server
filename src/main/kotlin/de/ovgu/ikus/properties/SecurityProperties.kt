package de.ovgu.ikus.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "security")
class SecurityProperties (val jwt: JwtProperties)