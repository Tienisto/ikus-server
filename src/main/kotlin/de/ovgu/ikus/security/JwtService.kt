package de.ovgu.ikus.security

import de.ovgu.ikus.BuildInfo
import de.ovgu.ikus.model.User
import de.ovgu.ikus.properties.AdminProperties
import de.ovgu.ikus.properties.JwtProperties
import de.ovgu.ikus.repository.UserRepo
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import java.lang.Exception
import java.util.*
import javax.crypto.spec.SecretKeySpec

data class AuthContext(val user: User, val isAdmin: Boolean)

enum class JwtStatus {
    OK, DEFAULT, TOO_SHORT
}

@Service
class JwtService (
        private val propsAdmin: AdminProperties,
        private val propsJwt: JwtProperties,
        private val userRepo: UserRepo
) {

    private val logger = LoggerFactory.getLogger(JwtService::class.java)
    private lateinit var jwtKey: SecretKeySpec
    private lateinit var jwtParser: JwtParser
    private lateinit var jwtParserApp: JwtParser

    @PostConstruct
    protected fun init() {
        val jwtKeyBytes = propsJwt.website.toByteArray()
        jwtKey = SecretKeySpec(jwtKeyBytes, 0, jwtKeyBytes.size, "HmacSHA256")
        jwtParser = Jwts.parserBuilder().setSigningKey(jwtKey).build()

        val jwtKeyBytesApp = propsJwt.app.toByteArray()
        jwtParserApp = Jwts
                .parserBuilder()
                .setSigningKey(SecretKeySpec(jwtKeyBytesApp, 0, jwtKeyBytesApp.size, "HmacSHA256"))
                .build()
    }

    fun getStatusWebsite(): JwtStatus {
        return when {
            propsJwt.website == BuildInfo.DEFAULT_PROPS["jwt.website"] -> JwtStatus.DEFAULT
            jwtKey.encoded.size * 8 < 256 -> JwtStatus.TOO_SHORT
            else -> JwtStatus.OK
        }
    }

    fun getStatusApp(): JwtStatus {
        val jwtKeyBytes = propsJwt.app.toByteArray()
        val jwtKey = SecretKeySpec(jwtKeyBytes, 0, jwtKeyBytes.size, "HmacSHA256")
        return when {
            propsJwt.app == BuildInfo.DEFAULT_PROPS["jwt.app"] -> JwtStatus.DEFAULT
            jwtKey.encoded.size * 8 < 256 -> JwtStatus.TOO_SHORT
            else -> JwtStatus.OK
        }
    }

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

    suspend fun getUser(token: String?) : User? {

        if (token == null)
            return null

        val userID: Int
        try {
            userID = jwtParser.parseClaimsJws(token).body["userID"] as Int
        } catch (e: Exception) {
            return null
        }
        return userRepo.findById(userID)
    }

    suspend fun checkAppToken(token: String?): Boolean {
        if (token == null)
            return false

        try {
            jwtParserApp.parseClaimsJws(token)
            return true
        } catch (e: Exception) {
            logger.info(e.message)
            return false
        }
    }
}

fun Authentication.toUser(): User {
    return (this.principal as AuthContext).user
}

fun Authentication.isAdmin(): Boolean {
    return (this.principal as AuthContext).isAdmin
}