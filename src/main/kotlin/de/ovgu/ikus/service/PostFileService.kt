package de.ovgu.ikus.service

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.model.PostFile
import de.ovgu.ikus.repository.PostFileRepo
import kotlinx.coroutines.flow.toList
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service

@Service
class PostFileService (
        private val postFileRepo: PostFileRepo,
        private val fileService: FileService
) {

    suspend fun findByIdIn(ids: List<Int>): List<PostFile> {
        return postFileRepo.findByIdIn(ids).toList()
    }

    suspend fun uploadFile(file: FilePart): PostFile {
        val originalFileName = file.filename()
        val extension = checkAndGetExtension(originalFileName)

        // save and get id
        val savedFile = postFileRepo.save(PostFile(null, null, originalFileName))

        // apply id to save to hard drive
        savedFile.fileName = "posts/${savedFile.id}.${extension}"
        fileService.storeFilePart(file, savedFile.fileName)
        return postFileRepo.save(savedFile) // update file name
    }

    private fun checkAndGetExtension(fileName: String): String {
        val lowerCase = fileName.toLowerCase()
        return when {
            lowerCase.endsWith(".jpg") || lowerCase.endsWith(".jpeg") -> "jpg"
            lowerCase.endsWith(".png") -> "png"
            else -> throw ErrorCode(409, "file type not allowed")
        }
    }
}