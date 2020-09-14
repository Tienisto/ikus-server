package de.ovgu.ikus.repository

import de.ovgu.ikus.model.PostFile
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PostFileRepo : CoroutineCrudRepository<PostFile, Int> {

    fun findByIdIn(ids: List<Int>): Flow<PostFile>
    fun findByPostIdIn(postIds: List<Int>): Flow<PostFile>
    fun findByPostId(postId: Int): Flow<PostFile>

    @Query("SELECT * FROM post_file WHERE post_id IS NULL")
    fun findUnusedFiles(): Flow<PostFile>
}