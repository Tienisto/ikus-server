package de.ovgu.ikus.repository

import de.ovgu.ikus.model.User
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepo : CoroutineCrudRepository<User, Int> {

    fun findByOrderByName(): Flow<User>
    suspend fun findByName(name: String): User?
}