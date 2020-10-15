package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Link
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LinkRepo : CoroutineCrudRepository<Link, Int> {

    @Query("SELECT MAX(position) FROM link WHERE channel_id = :channelId")
    suspend fun findMaxPositionByChannelId(channelId: Int): Int?

    @Query("SELECT * FROM link WHERE info ILIKE :info OR info_de ILIKE :info")
    suspend fun findByInfoIgnoreCase(info: String): List<Link>

    suspend fun findByOrderByPosition(): List<Link>
    suspend fun findByChannelIdOrderByPosition(channelId: Int): List<Link>
}