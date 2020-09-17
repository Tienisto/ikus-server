package de.ovgu.ikus.service

import de.ovgu.ikus.model.Channel
import de.ovgu.ikus.model.Event
import de.ovgu.ikus.repository.EventRepo
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class EventService (
        private val eventRepo: EventRepo
) {

    suspend fun findAllOrdered(): List<Event> {
        return eventRepo.findByOrderByStartTime().toList()
    }

    suspend fun findByChannelOrdered(channel: Channel): List<Event> {
        return eventRepo.findByChannelIdOrderByStartTime(channel).toList()
    }

    suspend fun findById(id: Int): Event? {
        return eventRepo.findById(id)
    }

    suspend fun save(event: Event): Event {
        return eventRepo.save(event)
    }

    // dummy only
    suspend fun saveAll(events: List<Event>): List<Event> {
        return eventRepo.saveAll(events).toList()
    }

    suspend fun delete(event: Event) {
        eventRepo.delete(event)
    }
}