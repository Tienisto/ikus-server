package de.ovgu.ikus.service

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.model.Audio
import de.ovgu.ikus.repository.AudioRepo
import kotlinx.coroutines.flow.collect
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service

@Service
class AudioService (
    private val audioRepo: AudioRepo,
    private val imageService: ImageService,
    private val fileService: FileService
) {

    suspend fun findAllOrdered(): List<Audio> {
        return audioRepo.findByOrderByPosition()
    }

    suspend fun findById(id: Int): Audio? {
        return audioRepo.findById(id)
    }

    suspend fun save(contact: Audio): Audio {
        return audioRepo.save(contact)
    }

    suspend fun saveAll(files: List<Audio>) {
        audioRepo.saveAll(files).collect()
    }

    suspend fun delete(audio: Audio) {
        deleteImages(audio)
        audioRepo.delete(audio)
    }

    suspend fun deleteAll() {
        audioRepo.deleteAll()
    }

    /**
     * stores the image to the hard drive and updates the file attribute of the audio
     */
    suspend fun setImage(audio: Audio, file: FilePart, locale: IkusLocale) {
        checkExtension(file.filename())

        val path = "audio/${audio.id}-$locale.jpg"
        val inputStream = imageService.digestImage(file, 2000, 2000)
        fileService.storeFileInputStream(inputStream, path)

        when (locale) {
            IkusLocale.EN -> audio.image = path
            IkusLocale.DE -> audio.imageDe = path
        }

        audioRepo.save(audio)
    }

    /**
     * deletes the images if existing, updates the audio
     */
    suspend fun deleteImages(audio: Audio) {
        val file = audio.image
        if (file != null)
            fileService.deleteFile(file)

        val fileDe = audio.imageDe
        if (fileDe != null)
            fileService.deleteFile(fileDe)

        audio.image = null
        audio.imageDe = null
        audioRepo.save(audio)
    }

    private fun checkExtension(fileName: String): String {
        val lowerCase = fileName.toLowerCase()
        return when {
            lowerCase.endsWith(".jpg") || lowerCase.endsWith(".jpeg") -> "jpg"
            lowerCase.endsWith(".png") -> "png"
            else -> throw ErrorCode(409, "file type not allowed")
        }
    }

    suspend fun findMaxPosition(): Int {
        return audioRepo.findMaxPosition() ?: -1
    }
}