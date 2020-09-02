package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Channel
import de.ovgu.ikus.model.ChannelType
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ChannelRepo : CoroutineCrudRepository<Channel, Int> {

    fun findByOrderByName(): Flow<Channel>
    fun findByTypeOrderByName(type: ChannelType): Flow<Channel>
}