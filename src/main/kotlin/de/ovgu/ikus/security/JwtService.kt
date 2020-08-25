package de.ovgu.ikus.security

import de.ovgu.ikus.model.User
import de.ovgu.ikus.properties.AdminProperties
import de.ovgu.ikus.properties.JwtProperties
import de.ovgu.ikus.repository.UserRepo
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import java.lang.Exception
import java.util.*

data class AuthContext(val user: User, val isAdmin: Boolean)

@Service
class JwtService (
        private val propsAdmin: AdminProperties,
        private val propsJwt: JwtProperties,
        private val userRepo: UserRepo
) {

    private val jwtKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)
    private val jwtParser = Jwts.parserBuilder().setSigningKey(jwtKey).build()

    fun createToken(user: User): String {
        val now = Date()
        val validity = Date(now.time + propsJwt.timeout)

        return Jwts.builder()
                .claim("userID", user.id)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(jwtKey)
                .compact()
    }

    suspend fun getAuthentication(token: String): Authentication? {
        val user = getUser(token) ?: return null
        val context = AuthContext(user, user.name == propsAdmin.name)
        return UsernamePasswordAuthenticationToken(context, "", listOf(SimpleGrantedAuthority("not important")))
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
    return (this.principal as AuthContext).user
}

fun Authentication.isAdmin(): Boolean {
    return (this.principal as AuthContext).isAdmin
}