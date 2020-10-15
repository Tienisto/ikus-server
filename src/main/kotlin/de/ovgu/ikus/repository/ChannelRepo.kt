package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Channel
import de.ovgu.ikus.model.ChannelType
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ChannelRepo : CoroutineCrudRepository<Channel, Int> {

    @Query("SELECT MAX(position) FROM channel WHERE type = :type")
    suspend fun findMaxPositionByType(type: ChannelType): Int?

    suspend fun findByOrderByPosition(): List<Channel>
    suspend fun findByType(type: ChannelType): List<Channel>
    suspend fun findByTypeOrderByPosition(type: ChannelType): List<Channel>
    suspend fun findByTypeOrderByName(type: ChannelType): List<Channel>
    suspend fun findByTypeOrderByNameDe(type: ChannelType): List<Channel>
}