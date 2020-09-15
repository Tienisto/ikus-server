package de.ovgu.ikus.service

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.model.Post
import de.ovgu.ikus.model.PostFile
import de.ovgu.ikus.repository.PostFileRepo
import de.ovgu.ikus.utils.toBytes
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
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
            true -> postFileRepo.findByPostIdIn(posts.map { post -> post.id!! }).toList()
            false -> emptyList()
        }
    }

    suspend fun findByPost(post: Post): List<PostFile> {
        return postFileRepo.findByPostId(post.id!!).toList()
    }

    suspend fun saveAll(files: List<PostFile>) {
        postFileRepo.saveAll(files).collect()
    }

    suspend fun uploadFile(file: FilePart): PostFile {
        val originalFileName = file.filename()
        checkExtension(originalFileName)

        // save and get id
        val savedFile = postFileRepo.save(PostFile(null, null, originalFileName))

        // apply id to save to hard drive
        savedFile.fileName = "posts/${savedFile.id}.jpg"

        val bytesOriginal = file.toBytes()

        val scaled = when (val rotated = imageService.digestImageRotation(bytesOriginal)) {
            null -> imageService.reduceSizeOfFile(ByteArrayInputStream(bytesOriginal))
            else -> imageService.reduceSizeOfFile(ByteArrayInputStream(rotated.toByteArray()))
        }

        fileService.storeFileInputStream(ByteArrayInputStream(scaled.toByteArray()), savedFile.fileName)

        return postFileRepo.save(savedFile) // update file name
    }

    /**
     * delete unused files
     */
    suspend fun cleanup() {
        val now = OffsetDateTime.now()
        postFileRepo
                .findUnusedFiles()
                .toList()
                .filter { file -> Duration.between(file.timestamp, now) > Duration.ofHours(3) }
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