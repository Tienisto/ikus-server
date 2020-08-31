package de.ovgu.ikus.service

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.model.User
import de.ovgu.ikus.properties.AdminProperties
import de.ovgu.ikus.repository.UserRepo
import de.ovgu.ikus.security.HashService
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class UserService (
        private val propsAdmin: AdminProperties,
        private val hashService: HashService,
        private val userRepo: UserRepo
) {

    suspend fun findById(id: Int): User? {
        return userRepo.findById(id)
    }

    suspend fun findAllOrdered(): List<User> {
        return userRepo
                .findByOrderByName()
                .filter { user -> user.name != propsAdmin.name }
                .toList()
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
}