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
class PostController(
    private val logService: LogService,
    private val cacheService: CacheService,
    private val channelService: ChannelService,
    private val postService: PostService,
    private val postFileService: PostFileService
) {

    @GetMapping("/news/all")
    suspend fun getAllNews(): List<PostDto> {
        val channels = channelService.findByType(ChannelType.NEWS)
        val channelsDtoMap = channels.associate { channel -> channel.id to channel.toDto() }
        val posts = postService.findByTypeOrderByPositionDesc(PostType.NEWS)
        val files = postFileService.findByPostIn(posts)

        val postsDto = posts.map { post ->
            val channel = channelsDtoMap[post.channelId] ?: throw ErrorCode(500, "channel not found in cache map")
            val currFiles = files
                .filter { file -> file.postId == post.id }
                .map { file -> file.toDto() }
            post.toDto(channel, currFiles)
        }

        return postsDto
    }

    @GetMapping("/news/archived")
    suspend fun getArchivedNews(): List<PostDto> {
        val channels = channelService.findByType(ChannelType.NEWS)
        val channelsDtoMap = channels.associate { channel -> channel.id to channel.toDto() }
        val posts = postService.findArchivedOrderByDateDesc()
        val files = postFileService.findByPostIn(posts)

        val postsDto = posts.map { post ->
            val channel = channelsDtoMap[post.channelId] ?: throw ErrorCode(500, "channel not found in cache map")
            val currFiles = files
                .filter { file -> file.postId == post.id }
                .map { file -> file.toDto() }
            post.toDto(channel, currFiles)
        }

        return postsDto
    }

    @GetMapping("/news")
    suspend fun getNewsInfo(@RequestParam channelId: Int): List<PostDto> {
        val channels = channelService.findByType(ChannelType.NEWS)
        val channel =
            channels.firstOrNull { channel -> channel.id == channelId } ?: throw ErrorCode(404, "Channel not found")
        val posts = postService.findByChannelIdOrderByPositionDesc(channel.id)
        val files = postFileService.findByPostIn(posts)

        val postsDto = posts.map { post ->
            val currFiles = files
                .filter { file -> file.postId == post.id }
                .map { file -> file.toDto() }
            post.toDto(channel.toDto(), currFiles)
        }

        return postsDto
    }

    @GetMapping("/faq")
    suspend fun getFaq(): List<PostGroupDto> {
        val type = PostType.FAQ
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

    @GetMapping("/search")
    suspend fun getBySearch(@RequestParam query: String): List<PostDto> {
        val channels = channelService.findByType(ChannelType.NEWS)
        val channelsDtoMap = channels.associate { channel -> channel.id to channel.toDto() }
        val posts = postService.search(query, PostType.NEWS)
        val files = postFileService.findByPostIn(posts)

        return posts.map { post ->
            val currFiles = files
                .filter { file -> file.postId == post.id }
                .map { file -> file.toDto() }
            val channel = channelsDtoMap[post.channelId] ?: ChannelDto(0, MultiLocaleString("ERROR", "ERROR"))
            post.toDto(channel, currFiles)
        }
    }

    @PostMapping
    suspend fun createPost(authentication: Authentication, @RequestBody request: Request.CreatePost) {
        val channel = channelService.findById(request.channelId) ?: throw ErrorCode(404, "Channel not found")
        val postType = channel.type.toPostType() ?: throw ErrorCode(409, "Wrong Channel Type")
        val maxPosition = when (postType) {
            PostType.NEWS -> postService.findMaxPositionByType(postType)
            PostType.FAQ -> postService.findMaxPositionByChannel(channel)
        }
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
        if (request.date != null) {
            post.date = request.date
        }

        val files = postFileService.findByIdIn(request.files)

        postService.save(post, files)
        logService.log(LogType.UPDATE_POST, authentication.toUser(), "${post.title} (${post.titleDe})")
        triggerUpdateFlag(post.type)
    }

    @PostMapping("/toggle-archive")
    suspend fun toggleArchive(authentication: Authentication, @RequestParam postId: Int) {
        val post = postService.findById(postId) ?: throw ErrorCode(404, "Post not found")
        if (post.type != PostType.NEWS)
            throw ErrorCode(409, "Wrong Type")

        post.archived = !post.archived

        if (!post.archived) {
            post.position = postService.findMaxPositionByType(PostType.NEWS) + 1
        }

        postService.saveSimple(post)
        logService.log(
            if (post.archived) LogType.ARCHIVE_POST else LogType.UNARCHIVE_POST,
            authentication.toUser(),
            "${post.title} (${post.titleDe})"
        )
        triggerUpdateFlag(post.type)
    }

    @PostMapping("/move-up")
    suspend fun moveUp(authentication: Authentication, @RequestBody request: Request.Id) {
        val post = postService.findById(request.id) ?: throw ErrorCode(404, "Post not found")
        val posts = when (post.type) {
            PostType.NEWS -> postService.findByTypeOrderByPosition(PostType.NEWS)
            PostType.FAQ -> postService.findByChannelIdOrderByPosition(post.channelId)
        }
        val reorderedPosts = posts
            .moveUpItem(
                item = post,
                equals = { a, b -> a.id == b.id },
                setIndex = { item, index -> item.position = index })

        if (reorderedPosts != null) {
            postService.saveAll(reorderedPosts)
            triggerUpdateFlag(post.type)
        }
    }

    @PostMapping("/move-down")
    suspend fun moveDown(authentication: Authentication, @RequestBody request: Request.Id) {
        val post = postService.findById(request.id) ?: throw ErrorCode(404, "Post not found")
        val posts = when (post.type) {
            PostType.NEWS -> postService.findByTypeOrderByPosition(PostType.NEWS)
            PostType.FAQ -> postService.findByChannelIdOrderByPosition(post.channelId)
        }
        val reorderedPosts = posts
            .moveDownItem(
                item = post,
                equals = { a, b -> a.id == b.id },
                setIndex = { item, index -> item.position = index })

        if (reorderedPosts != null) {
            postService.saveAll(reorderedPosts)
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