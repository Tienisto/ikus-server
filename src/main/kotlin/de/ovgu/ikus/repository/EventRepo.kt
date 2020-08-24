package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Event
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface EventRepo : CoroutineCrudRepository<Event, Int>