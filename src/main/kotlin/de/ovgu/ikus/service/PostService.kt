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

    // news (order by pinned > date > id)

    suspend fun findByChannelOrderByDate(channel: Channel, limit: Int): List<Post> {
        return postRepo.findByChannelIdOrderByDateDesc(channel.id, limit).toList()
    }

    suspend fun findByChannelOrderByPinnedDescDateDesc(channel: Channel): List<Post> {
        return postRepo.findByChannelIdOrderByPinnedDescDateDescIdDesc(channel.id).toList()
    }

    suspend fun findByTypeOrderByPinnedDescDateDesc(type: PostType): List<Post> {
        return postRepo.findByTypeOrderByPinnedDescDateDescIdDesc(type).toList()
    }

    suspend fun findPinnedOrderByDate(): List<Post> {
        return postRepo.findByPinnedOrderByDateDescIdDesc(true).toList()
    }

    suspend fun findByTypeOrderByDateLimited(type: PostType, limit: Int): List<Post> {
        return postRepo.findByOrderByDateDesc(type, limit).toList()
    }

    // faq (order by position)

    suspend fun findByTypeOrderByPosition(type: PostType): List<Post> {
        return postRepo.findByTypeOrderByPosition(type).toList()
    }


    suspend fun findByChannelIdOrderByPosition(channelId: Int): List<Post> {
        return postRepo.findByChannelIdOrderByPosition(channelId).toList()
    }

    suspend fun search(title: String, type: PostType): List<Post> {
        return postRepo.findByTypeAndTitleIgnoreCaseOrderByDate("%$title%", type).toList()
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
}