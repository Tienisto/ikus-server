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
    suspend fun findByTypeOrderByPosition(type: PostType): List<Post> {
        return postRepo.findByTypeAndArchivedOrderByPosition(type, false)
    }

    suspend fun findByTypeOrderByPositionDesc(type: PostType): List<Post> {
        return postRepo.findByTypeAndArchivedOrderByPositionDesc(type, false)
    }

    suspend fun findByTypeOrderByDateLimited(type: PostType, limit: Int): List<Post> {
        return postRepo.findByOrderByDateDesc(type, limit)
    }

    suspend fun findByChannelIdOrderByPosition(channelId: Int): List<Post> {
        return postRepo.findByChannelIdAndArchivedOrderByPosition(channelId, false)
    }

    suspend fun findByChannelIdOrderByPositionDesc(channelId: Int): List<Post> {
        return postRepo.findByChannelIdAndArchivedOrderByPositionDesc(channelId, false)
    }

    suspend fun findByChannelIdOrderByPositionLimited(channelId: Int, limit: Int): List<Post> {
        return postRepo.findByChannelIdOrderByPositionDesc(channelId, limit)
    }

    suspend fun findArchivedOrderByDateDesc(): List<Post> {
        return postRepo.findByTypeAndArchivedOrderByDateDesc(PostType.NEWS, true)
    }

    suspend fun search(title: String, type: PostType): List<Post> {
        return postRepo.findByTypeAndTitleIgnoreCaseOrderByDate("%$title%", type)
    }

    suspend fun findById(id: Int): Post? {
        return postRepo.findById(id)
    }

    suspend fun existsById(id: Int): Boolean {
        return postRepo.existsById(id)
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

    suspend fun saveSimple(post: Post) {
        postRepo.save(post)
    }

    // dummy only or when changing order
    suspend fun saveAll(posts: List<Post>): List<Post> {
        return postRepo.saveAll(posts).toList()
    }

    suspend fun delete(post: Post) {
        postRepo.delete(post)
    }

    suspend fun findMaxPositionByChannel(channel: Channel): Int {
        return postRepo.findMaxPositionByChannelId(channel.id) ?: -1
    }

    suspend fun findMaxPositionByType(type: PostType): Int {
        return postRepo.findMaxPositionByType(type) ?: -1
    }
}
