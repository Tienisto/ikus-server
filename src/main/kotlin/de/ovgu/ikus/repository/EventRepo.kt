package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Channel
import de.ovgu.ikus.model.Event
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface EventRepo : CoroutineCrudRepository<Event, Int> {

    suspend fun findByOrderByStartTime(): List<Event>
    suspend fun findByChannelIdOrderByStartTime(channel: Channel): List<Event>
}