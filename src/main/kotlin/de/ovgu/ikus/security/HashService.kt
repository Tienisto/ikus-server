package de.ovgu.ikus.security

import de.ovgu.ikus.properties.SecurityProperties
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service

/**
 * hashes the strings and checks them
 */

@Service
class HashService (private val properties: SecurityProperties) {

    /**
     * checks whether or not the string matches the hash
     * @param input the string to be checked
     * @param hash the hashed string
     * @return true if hash(input) == hash, otherwise false
     */
    fun check(input: String, hash: String): Boolean {
        return BCrypt.checkpw(input, hash)
    }

    /**
     * hashes the string
     * @param input the string to be hashed
     * @return the hashed string
     */
    fun hash(input: String): String {
        return BCrypt.hashpw(input, BCrypt.gensalt(properties.bcryptStrength))
    }
}