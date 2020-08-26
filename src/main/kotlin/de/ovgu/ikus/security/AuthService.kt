package de.ovgu.ikus.security

import de.ovgu.ikus.repository.UserRepo
import org.springframework.stereotype.Service

@Service
class AuthService (
        private val hashService: HashService,
        private val jwtService: JwtService,
        private val userRepo: UserRepo
) {

    /**
     * checks if the login credentials are correct
     * @return jwt token if correct, null otherwise
     */
    suspend fun login(name: String, password: String): String? {

        val user = userRepo.findByName(name) ?: return null

        if (!hashService.check(password, user.password))
            return null

        return jwtService.createToken(user)
    }
}