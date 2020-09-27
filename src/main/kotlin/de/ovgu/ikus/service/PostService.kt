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

    suspend fun findByTypeOrderByPosition(type: PostType): List<Post> {
        return postRepo.findByTypeOrderByPosition(type).toList()
    }

    suspend fun findByChannelOrderByDate(channel: Channel): List<Post> {
        return postRepo.findByChannelIdOrderByDateDesc(channel.id).toList()
    }

    suspend fun findByChannelOrderByDate(channel: Channel, limit: Int): List<Post> {
        return postRepo.findByChannelIdOrderByDateDesc(channel.id, limit).toList()
    }

    suspend fun findByChannelIdOrderByPosition(channelId: Int): List<Post> {
        return postRepo.findByChannelIdOrderByPosition(channelId).toList()
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

    // dummy only or when changing order
    suspend fun saveAll(posts: List<Post>): List<Post> {
        return postRepo.saveAll(posts).toList()
    }

    suspend fun delete(post: Post) {
        postRepo.delete(post)
    }

    // TODO: remove counting when NPE is fixed
    suspend fun findMaxPositionByChannel(channel: Channel): Int {
        if (postRepo.countByChannelId(channel.id) == 0)
            return -1
        return postRepo.findMaxPositionByChannelId(channel.id) ?: -1
    }
}