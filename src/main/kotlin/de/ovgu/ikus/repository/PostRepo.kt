package de.ovgu.ikus.repository

import de.ovgu.ikus.model.Channel
import de.ovgu.ikus.model.Post
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PostRepo : CoroutineCrudRepository<Post, Int> {

    fun findByChannelIdOrderByDateDesc(channel: Channel): Flow<Post>
}