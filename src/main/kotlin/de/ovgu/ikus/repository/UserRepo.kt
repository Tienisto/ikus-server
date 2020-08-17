package de.ovgu.ikus.repository

import de.ovgu.ikus.model.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepo : CoroutineCrudRepository<User, Int> {

    suspend fun findByName(name: String): User?
}