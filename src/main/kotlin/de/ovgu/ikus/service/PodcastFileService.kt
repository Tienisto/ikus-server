package de.ovgu.ikus.service

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.model.*
import de.ovgu.ikus.repository.PodcastFileRepo
import kotlinx.coroutines.flow.collect
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service

@Service
class PodcastFileService(
        private val podcastFileRepo: PodcastFileRepo,
        private val fileService: FileService
) {

    suspend fun findAllOrdered(): List<PodcastFile> {
        return podcastFileRepo.findByOrderByPosition()
    }

    suspend fun findById(id: Int): PodcastFile? {
        return podcastFileRepo.findById(id)
    }

    suspend fun save(file: PodcastFile): PodcastFile {
        return podcastFileRepo.save(file)
    }

    suspend fun saveAll(files: List<PodcastFile>) {
        podcastFileRepo.saveAll(files).collect()
    }

    suspend fun delete(file: PodcastFile) {
        fileService.deleteFile(file.file)
        fileService.deleteFile(file.fileDe)
        podcastFileRepo.delete(file)
    }

    suspend fun findMaxPosition(): Int {
        return podcastFileRepo.findMaxPosition() ?: -1
    }

    /**
     * stores the audio file to the hard drive and updates the file attribute of the podcast file
     */
    suspend fun setAudio(podcastFile: PodcastFile, filePart: FilePart, locale: IkusLocale) {
        checkExtension(filePart.filename())

        val path = "podcasts/audio/${podcastFile.id}-$locale.jpg"
        fileService.storeFilePart(filePart, path)

        when (locale) {
            IkusLocale.EN -> podcastFile.file = path
            IkusLocale.DE -> podcastFile.fileDe = path
        }

        podcastFileRepo.save(podcastFile)
    }

    private fun checkExtension(fileName: String): String {
        // TODO
        val lowerCase = fileName.toLowerCase()
        return when {
            lowerCase.endsWith(".mp3") -> "mp3"
            else -> throw ErrorCode(409, "file type not allowed")
        }
    }
}