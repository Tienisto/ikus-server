package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

@Table("ikus_user")
data class User(@Id var id: Int = 0,
                var name: String = "",
                var password: String = "",
                var accessPosts: Boolean = false,
                var accessEvents: Boolean = false,
                var accessLinks: Boolean = false,
                var accessHandBook: Boolean = false)

interface UserRepo : CoroutineCrudRepository<User, Int> {

    suspend fun findByName(name: String): User?
}