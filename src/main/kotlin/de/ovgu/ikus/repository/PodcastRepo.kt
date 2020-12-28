package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Podcast
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PodcastRepo : CoroutineCrudRepository<Podcast, Int> {

    @Query("SELECT MAX(position) FROM podcast")
    suspend fun findMaxPosition(): Int?

    suspend fun findByOrderByPosition(): List<Podcast>
}