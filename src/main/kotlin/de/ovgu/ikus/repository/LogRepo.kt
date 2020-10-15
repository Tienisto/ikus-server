package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Log
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LogRepo : CoroutineCrudRepository<Log, Int> {

    @Query("SELECT * FROM log ORDER BY timestamp DESC LIMIT :limit")
    suspend fun findAllWithLimit(limit: Int): List<Log>
}