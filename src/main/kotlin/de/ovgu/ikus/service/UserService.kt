package de.ovgu.ikus.service

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.model.User
import de.ovgu.ikus.properties.AdminProperties
import de.ovgu.ikus.repository.UserRepo
import de.ovgu.ikus.security.HashService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserService (
        private val propsAdmin: AdminProperties,
        private val hashService: HashService,
        private val userRepo: UserRepo
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    /**
     * ensure that there is an admin user with the correct password
     */
    suspend fun repairAdminAccount() {
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

    suspend fun findById(id: Int): User? {
        return userRepo.findById(id)
    }

    suspend fun findAllOrdered(): List<User> {
        return userRepo
                .findByOrderByName()
                .filter { user -> user.name != propsAdmin.name }
    }

    suspend fun addUser(name: String, password: String): User {

        if (userRepo.findByName(name) != null)
            throw ErrorCode(409, "Name already used")

        return userRepo.save(User(name = name, password = hashService.hash(password)))
    }

    suspend fun updateName(user: User, name: String): User {
        if (user.name != name && userRepo.findByName(name) != null) {
            // name changed
            throw ErrorCode(409, "Name already used")
        }

        user.name = name
        return userRepo.save(user)
    }

    suspend fun updatePassword(user: User, password: String): User {
        user.password = hashService.hash(password)
        return userRepo.save(user)
    }

    suspend fun delete(user: User) {
        userRepo.delete(user)
    }

    suspend fun deleteAll() {
        userRepo.deleteAll()
    }
}