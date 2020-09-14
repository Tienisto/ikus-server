package de.ovgu.ikus.service

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.model.Channel
import de.ovgu.ikus.model.Post
import de.ovgu.ikus.model.PostFile
import de.ovgu.ikus.repository.PostFileRepo
import de.ovgu.ikus.repository.PostRepo
import kotlinx.coroutines.flow.toList
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service

@Service
class PostService (
        private val fileService: FileService,
        private val postRepo: PostRepo,
        private val postFileRepo: PostFileRepo
) {

    suspend fun findAllOrdered(limit: Int): List<Post> {
        return postRepo.findByOrderByDateDesc(limit).toList()
    }

    suspend fun findByChannelOrdered(channel: Channel): List<Post> {
        return postRepo.findByChannelIdOrderByDateDesc(channel).toList()
    }

    suspend fun findByChannelOrdered(channel: Channel, limit: Int): List<Post> {
        return postRepo.findByChannelIdOrderByDateDesc(channel).toList()
    }

    suspend fun findById(id: Int): Post? {
        return postRepo.findById(id)
    }

    suspend fun save(post: Post): Post {
        return postRepo.save(post)
    }

    suspend fun saveAll(posts: List<Post>): List<Post> {
        return postRepo.saveAll(posts).toList()
    }

    suspend fun delete(post: Post) {
        postRepo.delete(post)
    }

    suspend fun uploadFile(file: FilePart) {
        val originalFileName = file.filename()
        val extension = checkAndGetExtension(originalFileName)
        val contentType = getMime(extension)

        // save and get id
        val savedFile = postFileRepo.save(PostFile(null, null, originalFileName, contentType, 0))

        // apply id to save to hard drive
        savedFile.fileName = "posts/${savedFile.id}.${extension}"
        fileService.storeFilePart(file, savedFile.fileName)
        postFileRepo.save(savedFile) // update file name
    }

    private fun checkAndGetExtension(fileName: String): String {
        val lowerCase = fileName.toLowerCase()
        return when {
            lowerCase.endsWith(".jpg") || lowerCase.endsWith(".jpeg") -> "jpg"
            lowerCase.endsWith(".png") -> "png"
            else -> throw ErrorCode(409, "file type not allowed")
        }
    }

    private fun getMime(extension: String): String {
        return when (extension.toLowerCase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            else -> throw ErrorCode(409, "file type not allowed")
        }
    }
}