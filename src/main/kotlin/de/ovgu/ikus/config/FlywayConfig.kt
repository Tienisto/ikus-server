package de.ovgu.ikus.config

import de.ovgu.ikus.properties.DBProperties
import de.ovgu.ikus.service.FeatureService
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
        private val userService: UserService,
        private val featureService: FeatureService
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
                            userService.repairAdminAccount() // ensure that there is an admin user
                            featureService.repairNativeFeatures() // ensure that there are all native features
                        }
                    }
                })
        return Flyway(config)
    }
}