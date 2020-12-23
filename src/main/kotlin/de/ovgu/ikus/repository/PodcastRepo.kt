package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Podcast
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PodcastRepo : CoroutineCrudRepository<Podcast, Int> {

    suspend fun findByOrderByPosition(): List<Podcast>
}