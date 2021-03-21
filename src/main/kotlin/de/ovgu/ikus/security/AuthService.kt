package de.ovgu.ikus.security

import de.ovgu.ikus.model.User
import de.ovgu.ikus.repository.UserRepo
import org.springframework.stereotype.Service

@Service
class AuthService (
    private val cryptoUtils: CryptoUtils,
    private val userRepo: UserRepo
) {

    /**
     * checks if the login credentials are correct
     * @return user if correct, null otherwise
     */
    suspend fun loginChallenge(name: String, password: String): User? {

        val user = userRepo.findByName(name) ?: return null

        if (!cryptoUtils.checkBcrypt(password, user.password))
            return null

        return user
    }
}