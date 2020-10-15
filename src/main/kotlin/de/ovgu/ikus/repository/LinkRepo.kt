package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Link
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LinkRepo : CoroutineCrudRepository<Link, Int> {

    @Query("SELECT MAX(position) FROM link WHERE channel_id = :channelId")
    suspend fun findMaxPositionByChannelId(channelId: Int): Int?

    @Query("SELECT * FROM link WHERE info ILIKE :info OR info_de ILIKE :info")
    fun findByInfoIgnoreCase(info: String): Flow<Link>

    fun findByOrderByPosition(): Flow<Link>
    fun findByChannelIdOrderByPosition(channelId: Int): Flow<Link>
}