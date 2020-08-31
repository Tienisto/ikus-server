package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Channel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ChannelRepo : CoroutineCrudRepository<Channel, Int> {

    fun findByOrderByName(): Flow<Channel>
}