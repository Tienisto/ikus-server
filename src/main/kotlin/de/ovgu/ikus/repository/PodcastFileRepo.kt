package de.ovgu.ikus.repository

import de.ovgu.ikus.model.PodcastFile
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PodcastFileRepo : CoroutineCrudRepository<PodcastFile, Int> {

    @Query("SELECT MAX(position) FROM podcast_file")
    suspend fun findMaxPosition(): Int?

    suspend fun findByOrderByPosition(): List<PodcastFile>
}