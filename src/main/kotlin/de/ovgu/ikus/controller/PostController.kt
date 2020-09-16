package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.*
import de.ovgu.ikus.model.*
import de.ovgu.ikus.security.toUser
import de.ovgu.ikus.service.*
import de.ovgu.ikus.utils.toChannelType
import de.ovgu.ikus.utils.toDto
import de.ovgu.ikus.utils.toPostType
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.LocalDate

@RestController
@RequestMapping("/api/posts")
class PostController (
        private val logService: LogService,
        private val cacheService: CacheService,
        private val channelService: ChannelService,
        private val postService: PostService,
        private val postFileService: PostFileService
) {

    @GetMapping
    suspend fun getByChannel(@RequestParam channelId: Int): List<PostDto> {
        val channel = channelService.findById(channelId) ?: throw ErrorCode(404, "Channel not found")
        val channelDto = channel.toDto()

        return postService
                .findByChannelOrdered(channel)
                .map { post ->
                    val files = postFileService
                            .findByPost(post)
                            .map { file -> file.toDto() }
                    post.toDto(channelDto, files)
                }
    }

    @GetMapping("/grouped")
    suspend fun getByType(@RequestParam type: PostType): List<PostGroupDto> {
        val posts = postService.findByTypeOrderByTitle(type)
        val channels = channelService.findByTypeOrdered(type.toChannelType())
        val files = postFileService.findByPostIn(posts)

        return channels.map { channel ->
            val channelDto = channel.toDto()
            val postsDto = posts
                    .filter { post -> post.channelId == channel.id }
                    .map { post ->
                        val currFiles = files
                                .filter { file -> file.postId == post.id }
                                .map { file -> file.toDto() }
                        post.toDto(channelDto, currFiles)
                    }
            PostGroupDto(channelDto, postsDto)
        }
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

        val files = postFileService.findByIdIn(request.files)

        postService.save(post, files)
        logService.log(LogType.CREATE_POST, authentication.toUser(), "${post.title} (${post.titleDe})")

        when (post.type) {
            PostType.NEWS -> cacheService.triggerUpdateFlag(CacheKey.NEWS)
            PostType.FAQ -> {}
        }
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

        val files = postFileService.findByIdIn(request.files)

        postService.save(post, files)
        logService.log(LogType.UPDATE_POST, authentication.toUser(), "${post.title} (${post.titleDe})")

        when (post.type) {
            PostType.NEWS -> cacheService.triggerUpdateFlag(CacheKey.NEWS)
            PostType.FAQ -> {}
        }
    }

    @DeleteMapping
    suspend fun deletePost(authentication: Authentication, @RequestBody request: Request.Id) {
        val post = postService.findById(request.id) ?: throw ErrorCode(404, "Post not found")
        postService.delete(post)
        logService.log(LogType.DELETE_POST, authentication.toUser(), "${post.title} (${post.titleDe})")

        when (post.type) {
            PostType.NEWS -> cacheService.triggerUpdateFlag(CacheKey.NEWS)
            PostType.FAQ -> {}
        }
    }

    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun upload(@RequestPart("file") file: Mono<FilePart>): PostFile {
        return postFileService.uploadFile(file.awaitFirst())
    }
}