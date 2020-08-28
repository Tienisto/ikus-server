package de.ovgu.ikus.service

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.model.User
import de.ovgu.ikus.repository.UserRepo
import de.ovgu.ikus.security.HashService
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class UserService (
        private val hashService: HashService,
        private val userRepo: UserRepo
) {

    suspend fun findById(id: Int): User? {
        return userRepo.findById(id)
    }

    suspend fun findAll(): List<User> {
        return userRepo.findAll().toList()
    }

    suspend fun addUser(name: String, password: String): User {

        if (userRepo.existsByName(name))
            throw ErrorCode(409, "Name already used")

        return userRepo.save(User(name = name, password = hashService.hash(password)))
    }

    suspend fun updateUser(userId: Int, name: String, password: String): User {
        val user = userRepo.findById(userId) ?: throw ErrorCode(404, "user not found")

        if (user.name != name && userRepo.existsByName(name)) {
            // name changed
            throw ErrorCode(409, "Name already used")
        }

        user.name = name
        user.password = hashService.hash(password)
        return userRepo.save(user)
    }

    suspend fun deleteById(userId: Int) {
        val user = userRepo.findById(userId) ?: throw ErrorCode(404, "user not found")
        userRepo.delete(user)
    }
}