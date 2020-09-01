package de.ovgu.ikus.service

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.model.Channel
import de.ovgu.ikus.model.ChannelType
import de.ovgu.ikus.model.Post
import de.ovgu.ikus.model.PostType
import de.ovgu.ikus.repository.PostRepo
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class PostService (
        private val postRepo: PostRepo
) {

    suspend fun findByChannelOrdered(channel: Channel): List<Post> {
        return postRepo.findByChannelIdOrderByDateDesc(channel).toList()
    }

    suspend fun findById(id: Int): Post? {
        return postRepo.findById(id)
    }

    suspend fun save(post: Post): Post {
        return postRepo.save(post)
    }

    suspend fun delete(post: Post) {
        postRepo.delete(post)
    }

    fun toPostType(channelType: ChannelType): PostType {
        return when (channelType) {
            ChannelType.NEWS -> PostType.NEWS
            ChannelType.FAQ -> PostType.FAQ
            ChannelType.EVENT -> throw ErrorCode(409, "Channel cannot have type EVENT")
        }
    }
}