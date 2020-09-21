package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Link
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LinkRepo : CoroutineCrudRepository<Link, Int> {

    fun findByOrderByInfo(): Flow<Link>
    fun findByOrderByInfoDe(): Flow<Link>
}