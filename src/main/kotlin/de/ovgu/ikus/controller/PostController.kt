package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.*
import de.ovgu.ikus.model.*
import de.ovgu.ikus.security.toUser
import de.ovgu.ikus.service.ChannelService
import de.ovgu.ikus.service.LogService
import de.ovgu.ikus.service.PostService
import de.ovgu.ikus.utils.toDto
import de.ovgu.ikus.utils.toPostType
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/posts")
class PostController (
        private val logService: LogService,
        private val channelService: ChannelService,
        private val postService: PostService
) {

    @GetMapping
    suspend fun getByChannel(@RequestParam channelId: Int): List<PostDto> {
        val channel = channelService.findById(channelId) ?: throw ErrorCode(404, "Channel not found")
        val channelDto = channel.toDto()

        return postService
                .findByChannelOrdered(channel)
                .map { post -> post.toDto(channelDto) }
    }

    @PostMapping
    suspend fun createPost(authentication: Authentication, @RequestBody request: Request.CreatePost) {
        val channel = channelService.findById(request.channelId) ?: throw ErrorCode(404, "Channel not found")
        val postType = channel.type.toPostType() ?: throw ErrorCode(409, "Channel cannot have type EVENT")
        val post = Post(
                type = postType, channelId = channel.id, date = LocalDate.now(),
                title = request.title.en.trim(), titleDe = request.title.de.trim(),
                content = request.content.en.trim(), contentDe = request.content.de.trim()
        )
        postService.save(post)
        logService.log(LogType.CREATE_POST, authentication.toUser(), "${post.title} (${post.titleDe})")
    }

    @PutMapping
    suspend fun updatePost(authentication: Authentication, @RequestBody request: Request.UpdatePost) {
        val post = postService.findById(request.id) ?: throw ErrorCode(404, "Post not found")
        val channel = channelService.findById(request.channelId) ?: throw ErrorCode(404, "Channel not found")

        // apply
        post.type = channel.type.toPostType() ?: throw ErrorCode(409, "Channel cannot have type EVENT")
        post.channelId = channel.id
        post.title = request.title.en.trim()
        post.titleDe = request.title.de.trim()
        post.content = request.content.en.trim()
        post.contentDe = request.content.de.trim()
        postService.save(post)
        logService.log(LogType.UPDATE_POST, authentication.toUser(), "${post.title} (${post.titleDe})")
    }

    @DeleteMapping
    suspend fun deletePost(authentication: Authentication, @RequestBody request: Request.Id) {
        val post = postService.findById(request.id) ?: throw ErrorCode(404, "Post not found")
        postService.delete(post)
        logService.log(LogType.DELETE_POST, authentication.toUser(), "${post.title} (${post.titleDe})")
    }
}