package de.ovgu.ikus.config

import de.ovgu.ikus.model.User
import de.ovgu.ikus.properties.AdminProperties
import de.ovgu.ikus.properties.DBProperties
import de.ovgu.ikus.repository.UserRepo
import de.ovgu.ikus.security.HashService
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.callback.BaseCallback
import org.flywaydb.core.api.callback.Context
import org.flywaydb.core.api.callback.Event
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// https://stackoverflow.com/a/61412233
@Configuration
class FlywayConfig (
        private val propsAdmin: AdminProperties,
        private val propsDB: DBProperties,
        private val hashService: HashService,
        private val userRepo: UserRepo
) {

    private val logger = LoggerFactory.getLogger(FlywayConfig::class.java)

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
                            var user = userRepo.findByName(propsAdmin.name)
                            if (user == null) {
                                // no admin found
                                user = User(name = propsAdmin.name, password = hashService.hash(propsAdmin.password))
                                userRepo.save(user)
                                logger.info("Created admin user.")
                            } else if (!hashService.check(propsAdmin.password, user.password)) {
                                // admin exists but wrong password
                                user.password = hashService.hash(propsAdmin.password)
                                userRepo.save(user)
                                logger.info("Updated admin user password.")
                            }
                        }
                    }
                })
        return Flyway(config)
    }
}