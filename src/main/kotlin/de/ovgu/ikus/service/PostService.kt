package de.ovgu.ikus.service

import de.ovgu.ikus.model.Post
import de.ovgu.ikus.model.PostType
import de.ovgu.ikus.repository.PostRepo
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class PostService (
        private val postRepo: PostRepo
) {

    suspend fun findByType(type: PostType): List<Post> {
        return postRepo.findByType(type).toList()
    }

    suspend fun save(post: Post): Post {
        return postRepo.save(post)
    }

    suspend fun delete(post: Post) {
        postRepo.delete(post)
    }
}