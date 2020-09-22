package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Contact
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ContactRepo : CoroutineCrudRepository<Contact, Int> {

    fun findByOrderByName(): Flow<Contact>
    fun findByOrderByNameDe(): Flow<Contact>
}