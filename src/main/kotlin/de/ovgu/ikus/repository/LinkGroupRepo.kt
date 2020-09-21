package de.ovgu.ikus.repository

import de.ovgu.ikus.model.LinkGroup
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LinkGroupRepo : CoroutineCrudRepository<LinkGroup, Int> {

    fun findByOrderByName(): Flow<LinkGroup>
    fun findByOrderByNameDe(): Flow<LinkGroup>
}