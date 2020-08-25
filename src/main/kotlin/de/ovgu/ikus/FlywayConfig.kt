package de.ovgu.ikus

import de.ovgu.ikus.properties.DBProperties
import org.flywaydb.core.Flyway
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// https://stackoverflow.com/a/61412233
@Configuration
class FlywayConfig (private val env: DBProperties) {

    @Bean(initMethod = "migrate")
    fun flyway(): Flyway {
        val url = "jdbc:" + env.url
        val user = env.user
        val password = env.password
        val config = Flyway.configure().dataSource(url, user, password)
        return Flyway(config)
    }
}