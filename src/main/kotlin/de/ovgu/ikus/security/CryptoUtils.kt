package de.ovgu.ikus.security

import de.ovgu.ikus.properties.HashingProperties
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service
import java.security.MessageDigest

/**
 * hashes the strings and checks them
 */

@Service
class CryptoUtils (private val properties: HashingProperties) {

    /**
     * checks whether or not the string matches the hash
     * @param input the string to be checked
     * @param hash the hashed string
     * @return true if hash(input) == hash, otherwise false
     */
    fun checkBcrypt(input: String, hash: String): Boolean {
        return BCrypt.checkpw(input, hash)
    }

    /**
     * hashes the string
     * @param input the string to be hashed
     * @return the hashed string
     */
    fun hashBcrypt(input: String): String {
        return BCrypt.hashpw(input, BCrypt.gensalt(properties.bcryptStrength))
    }

    /**
     * hashes the string using SHA-256
     * @param message the string to be hashed
     * @return the hashed string
     */
    fun hashSHA256(message: String): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(message.toByteArray())
    }
}

fun ByteArray.toHexString(): String {
    return joinToString("") { "%02x".format(it) }
}