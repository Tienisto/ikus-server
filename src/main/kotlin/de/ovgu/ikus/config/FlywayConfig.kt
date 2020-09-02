package de.ovgu.ikus.config

import de.ovgu.ikus.properties.DBProperties
import de.ovgu.ikus.service.UserService
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.callback.BaseCallback
import org.flywaydb.core.api.callback.Context
import org.flywaydb.core.api.callback.Event
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// https://stackoverflow.com/a/61412233
@Configuration
class FlywayConfig (
        private val propsDB: DBProperties,
        private val userService: UserService
) {

    @Bean(initMethod = "migrate")
    fun flyway(): Flyway {
        val config = Flyway
                .configure()
                .dataSource("jdbc:" + propsDB.url, propsDB.user, propsDB.password)
                .callbacks(object : BaseCallback() {
                    override fun supports(event: Event, context: Context): Boolean {
                        return event == Event.AFTER_MIGRATE
                    }

                    override fun handle(event: Event, context: Context) {
                        runBlocking {
                            // ensure that there is an admin user
                            userService.adminAccount()
                        }
                    }
                })
        return Flyway(config)
    }
}