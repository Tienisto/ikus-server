package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Channel
import de.ovgu.ikus.model.Event
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface EventRepo : CoroutineCrudRepository<Event, Int> {

    fun findByOrderByStartTime(): Flow<Event>
    fun findByChannelIdOrderByStartTime(channel: Channel): Flow<Event>
}