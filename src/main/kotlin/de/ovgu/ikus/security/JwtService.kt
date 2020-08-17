package de.ovgu.ikus.security

import de.ovgu.ikus.model.User
import de.ovgu.ikus.model.UserRepo
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.lang.Exception
import java.util.*

@Component
class JwtService (
        private val userRepo: UserRepo,

        @Value("\${security.jwt.timeout}")
        private var validityInMilliseconds: Long
) {

    private val jwtKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)
    private val jwtParser = Jwts.parserBuilder().setSigningKey(jwtKey).build()

    fun createToken(user: User): String {
        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)

        return Jwts.builder()
                .claim("userID", user.id)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(jwtKey)
                .compact()
    }

    suspend fun getAuthentication(token: String): Authentication? {
        val user = getUser(token) ?: return null
        return UsernamePasswordAuthenticationToken(user, "", listOf(SimpleGrantedAuthority("not important")))
    }

    suspend fun getUser(token: String) : User? {
        val userID: Int
        try {
            userID = jwtParser.parseClaimsJws(token).body["userID"] as Int
        } catch (e: Exception) {
            return null
        }
        return userRepo.findById(userID)
    }
}

fun Authentication.toUser(): User {
    return this.principal as User
}