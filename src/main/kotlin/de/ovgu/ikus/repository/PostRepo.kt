package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Channel
import de.ovgu.ikus.model.Post
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PostRepo : CoroutineCrudRepository<Post, Int> {

    @Query("SELECT * FROM post ORDER BY date DESC LIMIT :limit")
    fun findByOrderByDateDesc(limit: Int): Flow<Post>

    fun findByChannelIdOrderByDateDesc(channel: Channel): Flow<Post>
}