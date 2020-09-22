package de.ovgu.ikus.service

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.model.Post
import de.ovgu.ikus.model.PostFile
import de.ovgu.ikus.repository.PostFileRepo
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.OffsetDateTime

@Service
class PostFileService(
        private val postFileRepo: PostFileRepo,
        private val fileService: FileService,
        private val imageService: ImageService
) {

    suspend fun findById(id: Int): PostFile? {
        return postFileRepo.findById(id)
    }

    suspend fun findByIdIn(ids: List<Int>): List<PostFile> {
        return when (ids.isNotEmpty()) {
            true -> postFileRepo.findByIdIn(ids).toList()
            false -> emptyList()
        }
    }

    suspend fun findByPostIn(posts: List<Post>): List<PostFile> {
        return when (posts.isNotEmpty()) {
            true -> postFileRepo.findByPostIdIn(posts.map { post -> post.id }).toList()
            false -> emptyList()
        }
    }

    suspend fun findByPost(post: Post): List<PostFile> {
        return postFileRepo.findByPostId(post.id).toList()
    }

    suspend fun saveAll(files: List<PostFile>) {
        postFileRepo.saveAll(files).collect()
    }

    suspend fun uploadFile(file: FilePart): PostFile {
        checkExtension(file.filename())

        // save and get id
        val savedFile = postFileRepo.save(PostFile(fileName = "will be replaced", timestamp = OffsetDateTime.now()))

        // apply id to save to hard drive
        val inputStream = imageService.digestImage(file)
        val path = "posts/${savedFile.id}.jpg"
        fileService.storeFileInputStream(inputStream, path)
        savedFile.fileName = path
        return postFileRepo.save(savedFile) // update file name
    }

    /**
     * Delete unused files.
     * In a normal case, max age may be greater than zero to allow users to upload temporary files.
     * This happens when the user creates a new post.
     * @param maxAge the time passed since this file has been uploaded
     */
    suspend fun cleanup(maxAge: Duration = Duration.ofHours(3)) {
        val now = OffsetDateTime.now()
        postFileRepo
                .findUnusedFiles()
                .toList()
                .filter { file -> Duration.between(file.timestamp, now) > maxAge }
                .forEach { file ->
                    postFileRepo.delete(file)
                    fileService.deleteFile(file.fileName)
                }
    }

    private fun checkExtension(fileName: String): String {
        val lowerCase = fileName.toLowerCase()
        return when {
            lowerCase.endsWith(".jpg") || lowerCase.endsWith(".jpeg") -> "jpg"
            lowerCase.endsWith(".png") -> "png"
            else -> throw ErrorCode(409, "file type not allowed")
        }
    }
}