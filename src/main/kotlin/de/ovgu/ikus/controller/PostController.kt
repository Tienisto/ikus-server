package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.*
import de.ovgu.ikus.model.*
import de.ovgu.ikus.security.toUser
import de.ovgu.ikus.service.*
import de.ovgu.ikus.utils.*
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

    @GetMapping("/news")
    suspend fun getNewsInfo(@RequestParam channelId: Int): NewsDto {
        val channels = channelService.findByType(ChannelType.NEWS)
        val channel = channels.firstOrNull { channel -> channel.id == channelId } ?: throw ErrorCode(404, "Channel not found")
        val posts = postService.findByChannelOrderByPinnedDescDateDesc(channel)
        val files = postFileService.findByPostIn(posts)

        val postsDto = posts.map { post ->
            val currFiles = files
                    .filter { file -> file.postId == post.id }
                    .map { file -> file.toDto() }
            post.toDto(channel.toDto(), currFiles)
        }

        val pinned = postService.findPinnedOrderByDate().map { post ->
            when (val digestedPost = postsDto.firstOrNull { postDto -> postDto.id == post.id }) {
                null -> {
                    // fetch from database
                    val currChannel = channels.first { channel -> channel.id == post.channelId }.toDto()
                    val currFiles = postFileService.findByPost(post).map { file -> file.toDto() }
                    post.toDto(currChannel, currFiles)
                }
                else -> {
                    // use finished post
                    digestedPost
                }
            }
        }

        return NewsDto(postsDto, pinned)
    }

    @GetMapping("/grouped-order-position")
    suspend fun getByType(@RequestParam type: PostType): List<PostGroupDto> {
        val posts = postService.findByTypeOrderByPosition(type)
        val channels = channelService.findByTypeOrderByPosition(type.toChannelType())
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
        val postType = channel.type.toPostType() ?: throw ErrorCode(409, "Wrong Channel Type")
        val maxPosition = postService.findMaxPositionByChannel(channel)
        val post = Post(
                type = postType, channelId = channel.id, date = LocalDate.now(),
                title = request.title.en.trim(), titleDe = request.title.de.trim(),
                content = request.content.en.trim(), contentDe = request.content.de.trim(),
                position = maxPosition + 1
        )

        val files = postFileService.findByIdIn(request.files)

        postService.save(post, files)
        logService.log(LogType.CREATE_POST, authentication.toUser(), "${post.title} (${post.titleDe})")
        triggerUpdateFlag(postType)
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
        triggerUpdateFlag(post.type)
    }

    @PostMapping("/toggle-pin")
    suspend fun togglePin(authentication: Authentication, @RequestParam postId: Int) {
        val post = postService.findById(postId) ?: throw ErrorCode(404, "Post not found")
        if (post.type != PostType.NEWS)
            throw ErrorCode(409, "Wrong Type")

        post.pinned = !post.pinned
        postService.saveSimple(post)
        logService.log(if (post.pinned) LogType.PIN_POST else LogType.UNPIN_POST, authentication.toUser(), "${post.title} (${post.titleDe})")
        triggerUpdateFlag(post.type)
    }

    @PostMapping("/move-up")
    suspend fun moveUp(authentication: Authentication, @RequestBody request: Request.Id) {
        val post = postService.findById(request.id) ?: throw ErrorCode(404, "Post not found")
        val posts = postService
                .findByChannelIdOrderByPosition(post.channelId)
                .moveUpItem(item = post, getId = { item -> item.id }, setIndex = { item, index -> item.position = index })

        if (posts != null) {
            postService.saveAll(posts)
            triggerUpdateFlag(post.type)
        }
    }

    @PostMapping("/move-down")
    suspend fun moveDown(authentication: Authentication, @RequestBody request: Request.Id) {
        val post = postService.findById(request.id) ?: throw ErrorCode(404, "Post not found")
        val posts = postService
                .findByChannelIdOrderByPosition(post.channelId)
                .moveDownItem(item = post, getId = { item -> item.id }, setIndex = { item, index -> item.position = index })

        if (posts != null) {
            postService.saveAll(posts)
            triggerUpdateFlag(post.type)
        }
    }

    @DeleteMapping
    suspend fun deletePost(authentication: Authentication, @RequestBody request: Request.Id) {
        val post = postService.findById(request.id) ?: throw ErrorCode(404, "Post not found")
        postService.delete(post)
        logService.log(LogType.DELETE_POST, authentication.toUser(), "${post.title} (${post.titleDe})")
        triggerUpdateFlag(post.type)
    }

    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun upload(@RequestPart("file") file: Mono<FilePart>): PostFile {
        return postFileService.uploadFile(file.awaitFirst())
    }

    private fun triggerUpdateFlag(type: PostType) {
        when (type) {
            PostType.NEWS -> cacheService.triggerUpdateFlag(CacheKey.NEWS)
            PostType.FAQ -> cacheService.triggerUpdateFlag(CacheKey.FAQ)
        }
    }
}