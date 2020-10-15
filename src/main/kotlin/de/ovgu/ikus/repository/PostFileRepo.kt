package de.ovgu.ikus.repository

import de.ovgu.ikus.model.PostFile
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PostFileRepo : CoroutineCrudRepository<PostFile, Int> {

    suspend fun findByIdIn(ids: List<Int>): List<PostFile>
    suspend fun findByPostIdIn(postIds: List<Int>): List<PostFile>
    suspend fun findByPostId(postId: Int): List<PostFile>

    @Query("SELECT * FROM post_file WHERE post_id IS NULL")
    suspend fun findUnusedFiles(): List<PostFile>
}