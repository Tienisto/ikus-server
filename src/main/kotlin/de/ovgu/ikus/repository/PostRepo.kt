package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Post
import de.ovgu.ikus.model.PostType
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PostRepo : CoroutineCrudRepository<Post, Int> {

    fun findByType(type: PostType): Flow<Post>
}