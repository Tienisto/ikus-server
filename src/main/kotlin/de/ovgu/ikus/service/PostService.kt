package de.ovgu.ikus.service

import de.ovgu.ikus.model.Channel
import de.ovgu.ikus.model.Post
import de.ovgu.ikus.model.PostFile
import de.ovgu.ikus.model.PostType
import de.ovgu.ikus.repository.PostRepo
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class PostService (
        private val postRepo: PostRepo,
        private val postFileService: PostFileService
) {

    suspend fun findByTypeOrderByDateLimited(type: PostType, limit: Int): List<Post> {
        return postRepo.findByOrderByDateDesc(type.name, limit).toList()
    }

    suspend fun findByTypeOrderByTitle(type: PostType): List<Post> {
        return postRepo.findByTypeOrderByTitle(type).toList()
    }

    suspend fun findByChannelOrdered(channel: Channel): List<Post> {
        return postRepo.findByChannelIdOrderByDateDesc(channel).toList()
    }

    suspend fun findByChannelOrdered(channel: Channel, limit: Int): List<Post> {
        return postRepo.findByChannelIdOrderByDateDesc(channel).toList()
    }

    suspend fun findById(id: Int): Post? {
        return postRepo.findById(id)
    }

    suspend fun save(post: Post, files: List<PostFile>): Post {
        val saved = postRepo.save(post)

        // break or add connection to the post
        val allFiles = postFileService
                .findByPost(post)
                .plus(files)
                .distinctBy { file -> file.id }

        allFiles.forEach { file ->
            file.postId = when (files.any { f -> f.id == file.id }) {
                true -> saved.id
                false -> null
            }
        }

        postFileService.saveAll(allFiles)
        postFileService.cleanup()
        return saved
    }

    // dummy only
    suspend fun saveAll(posts: List<Post>): List<Post> {
        return postRepo.saveAll(posts).toList()
    }

    suspend fun delete(post: Post) {
        postRepo.delete(post)
    }
}