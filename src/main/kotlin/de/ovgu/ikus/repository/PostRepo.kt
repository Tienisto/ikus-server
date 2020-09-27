package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Post
import de.ovgu.ikus.model.PostType
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PostRepo : CoroutineCrudRepository<Post, Int> {

    suspend fun countByChannelId(channelId: Int): Int

    // TODO: use enum-native solution
    @Query("SELECT MAX(position) FROM post WHERE channel_id = :channelId")
    suspend fun findMaxPositionByChannelId(channelId: Int): Int?

    @Query("SELECT * FROM post WHERE type = :type ORDER BY date DESC LIMIT :limit")
    fun findByOrderByDateDesc(type: String, limit: Int): Flow<Post>

    @Query("SELECT * FROM post WHERE channel_id = :channelId ORDER BY date DESC LIMIT :limit")
    fun findByChannelIdOrderByDateDesc(channelId: Int, limit: Int): Flow<Post>

    fun findByChannelIdOrderByDateDesc(channelId: Int): Flow<Post>
    fun findByChannelIdOrderByPosition(channelId: Int): Flow<Post>
    fun findByTypeOrderByPosition(type: PostType): Flow<Post>
}