package de.ovgu.ikus.security

import de.ovgu.ikus.model.User
import de.ovgu.ikus.repository.UserRepo
import org.springframework.stereotype.Service

@Service
class AuthService (
        private val userRepo: UserRepo
) {

    suspend fun login(name: String, password: String): String? {
        userRepo.save(User(0, "Max", "Mustermann"))
        return ""
    }
}