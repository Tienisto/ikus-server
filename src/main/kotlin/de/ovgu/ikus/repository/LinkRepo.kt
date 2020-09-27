package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Link
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LinkRepo : CoroutineCrudRepository<Link, Int> {

    suspend fun countByChannelId(channelId: Int): Int

    // TODO: use enum-native solution
    @Query("SELECT MAX(position) FROM link WHERE channel_id = :channelId")
    suspend fun findMaxPositionByChannelId(channelId: Int): Int?

    fun findByOrderByPosition(): Flow<Link>
    fun findByChannelIdOrderByPosition(channelId: Int): Flow<Link>
}