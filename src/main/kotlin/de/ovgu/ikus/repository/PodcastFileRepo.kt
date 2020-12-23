package de.ovgu.ikus.repository

import de.ovgu.ikus.model.PodcastFile
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PodcastFileRepo : CoroutineCrudRepository<PodcastFile, Int> {

    suspend fun findByOrderByPosition(): List<PodcastFile>
}