package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Contact
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ContactRepo : CoroutineCrudRepository<Contact, Int> {

    @Query("SELECT MAX(position) FROM contact")
    suspend fun findMaxPosition(): Int?

    fun findByOrderByPosition(): Flow<Contact>
}