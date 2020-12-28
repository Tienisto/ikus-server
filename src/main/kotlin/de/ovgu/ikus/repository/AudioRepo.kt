package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Audio
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface AudioRepo : CoroutineCrudRepository<Audio, Int> {

    @Query("SELECT MAX(position) FROM audio")
    suspend fun findMaxPosition(): Int?

    suspend fun findByOrderByPosition(): List<Audio>
}