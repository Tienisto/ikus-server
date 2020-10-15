package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Post
import de.ovgu.ikus.model.PostType
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PostRepo : CoroutineCrudRepository<Post, Int> {

    @Query("SELECT MAX(position) FROM post WHERE channel_id = :channelId")
    suspend fun findMaxPositionByChannelId(channelId: Int): Int?

    @Query("SELECT * FROM post WHERE type = :type ORDER BY date DESC, id DESC LIMIT :limit")
    suspend fun findByOrderByDateDesc(type: PostType, limit: Int): List<Post>

    @Query("SELECT * FROM post WHERE channel_id = :channelId ORDER BY date DESC, id DESC LIMIT :limit")
    suspend fun findByChannelIdOrderByDateDesc(channelId: Int, limit: Int): List<Post>

    @Query("SELECT * FROM post WHERE type = :type AND (title ILIKE :title OR title_de ILIKE :title) ORDER BY date DESC, id DESC")
    suspend fun findByTypeAndTitleIgnoreCaseOrderByDate(title: String, type: PostType): List<Post>

    suspend fun findByPinnedOrderByDateDescIdDesc(pinned: Boolean): List<Post>
    suspend fun findByChannelIdOrderByPinnedDescDateDescIdDesc(channelId: Int): List<Post>
    suspend fun findByTypeOrderByPinnedDescDateDescIdDesc(type: PostType): List<Post>
    suspend fun findByChannelIdOrderByPosition(channelId: Int): List<Post>
    suspend fun findByTypeOrderByPosition(type: PostType): List<Post>
}