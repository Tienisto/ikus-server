package de.ovgu.ikus.service

import de.ovgu.ikus.model.Channel
import de.ovgu.ikus.model.Event
import de.ovgu.ikus.model.RegistrationData
import de.ovgu.ikus.repository.EventRepo
import de.ovgu.ikus.utils.parseJSON
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class EventService (
        private val eventRepo: EventRepo
) {

    suspend fun findAllOrdered(): List<Event> {
        return eventRepo.findByOrderByStartTime()
    }

    suspend fun findByChannelOrdered(channel: Channel): List<Event> {
        return eventRepo.findByChannelIdOrderByStartTime(channel)
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

    suspend fun removeRegisteredUser(event: Event, token: String): Boolean {
        val index = event.registrations
            .map { r -> r.parseJSON<RegistrationData>() }
            .indexOfFirst { r -> r.token == token }

        if (index != -1) {
            // remove registration from list
            event.registrations = event.registrations.filterIndexed { i, _ -> i != index }
            eventRepo.save(event)
            return true
        } else {
            return false
        }
    }
}