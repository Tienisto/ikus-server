package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Post
import de.ovgu.ikus.model.PostType
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PostRepo : CoroutineCrudRepository<Post, Int> {

    @Query("SELECT MAX(position) FROM post WHERE channel_id = :channelId AND archived = false")
    suspend fun findMaxPositionByChannelId(channelId: Int): Int?

    @Query("SELECT MAX(position) FROM post WHERE type = :type AND archived = false")
    suspend fun findMaxPositionByType(type: PostType): Int?

    @Query("SELECT * FROM post WHERE type = :type AND archived = false ORDER BY date DESC, id DESC LIMIT :limit")
    suspend fun findByOrderByDateDesc(type: PostType, limit: Int): List<Post>

    @Query("SELECT * FROM post WHERE channel_id = :channelId AND archived = false ORDER BY position DESC LIMIT :limit")
    suspend fun findByChannelIdOrderByPositionDesc(channelId: Int, limit: Int): List<Post>

    @Query("SELECT * FROM post WHERE type = :type AND (title ILIKE :title OR title_de ILIKE :title) ORDER BY date DESC, id DESC")
    suspend fun findByTypeAndTitleIgnoreCaseOrderByDate(title: String, type: PostType): List<Post>

    suspend fun findByChannelIdAndArchivedOrderByPosition(channelId: Int, archived: Boolean): List<Post>
    suspend fun findByChannelIdAndArchivedOrderByPositionDesc(channelId: Int, archived: Boolean): List<Post>
    suspend fun findByTypeAndArchivedOrderByPosition(type: PostType, archived: Boolean): List<Post>
    suspend fun findByTypeAndArchivedOrderByPositionDesc(type: PostType, archived: Boolean): List<Post>
    suspend fun findByTypeAndArchivedOrderByDateDesc(type: PostType, archived: Boolean): List<Post>
}
