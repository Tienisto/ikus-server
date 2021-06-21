package de.ovgu.ikus.service

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.model.*
import de.ovgu.ikus.repository.AudioFileRepo
import kotlinx.coroutines.flow.collect
import org.slf4j.LoggerFactory
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream

@Service
class AudioFileService(
    private val audioFileRepo: AudioFileRepo,
    private val fileService: FileService,
    private val imageService: ImageService
) {

    private val logger = LoggerFactory.getLogger(AudioFileService::class.java)

    suspend fun findAllOrdered(): List<AudioFile> {
        return audioFileRepo.findByOrderByPosition()
    }

    suspend fun findByAudioOrderByPosition(audioId: Int): List<AudioFile> {
        return audioFileRepo.findByAudioIdOrderByPosition(audioId)
    }

    suspend fun findById(id: Int): AudioFile? {
        return audioFileRepo.findById(id)
    }

    suspend fun save(file: AudioFile): AudioFile {
        return audioFileRepo.save(file)
    }

    suspend fun saveAll(files: List<AudioFile>) {
        audioFileRepo.saveAll(files).collect()
    }

    suspend fun delete(file: AudioFile) {
        try {
            // delete audio files
            fileService.deleteFile(file.file)
            fileService.deleteFile(file.fileDe)

            // delete image files
            deleteImages(file)
        } catch (e: Exception) {
            logger.error(e.message, e)
        }

        audioFileRepo.delete(file)
    }

    suspend fun findMaxPosition(): Int {
        return audioFileRepo.findMaxPosition() ?: -1
    }

    /**
     * stores the audio file to the hard drive and updates the file attribute of the audio file
     */
    suspend fun setAudio(audioFile: AudioFile, fileName: String, byteArray: ByteArray, locale: IkusLocale) {

        try {
            when (locale) {
                // file may be empty before first file has been uploaded
                IkusLocale.EN -> if (audioFile.file != "") fileService.deleteFile(audioFile.file)
                IkusLocale.DE -> if (audioFile.fileDe != "") fileService.deleteFile(audioFile.fileDe)
            }
        } catch (e: Exception) {
            logger.error(e.message, e)
        }

        val extension = checkExtensionAudio(fileName)
        val path = "audio/files/${audioFile.id}-$locale.$extension"
        fileService.storeFileInputStream(ByteArrayInputStream(byteArray), path)

        when (locale) {
            IkusLocale.EN -> audioFile.file = path
            IkusLocale.DE -> audioFile.fileDe = path
        }

        audioFileRepo.save(audioFile)
    }

    /**
     * stores the image to the hard drive and updates the file attribute of the audio
     */
    suspend fun setImage(audioFile: AudioFile, file: FilePart, locale: IkusLocale) {
        checkExtensionImage(file.filename())

        val path = "audio/files/${audioFile.id}-$locale.jpg"
        val inputStream = imageService.digestImage(file, 2000, 2000)
        fileService.storeFileInputStream(inputStream, path)

        when (locale) {
            IkusLocale.EN -> audioFile.image = path
            IkusLocale.DE -> audioFile.imageDe = path
        }

        audioFileRepo.save(audioFile)
    }

    /**
     * deletes the images if existing, updates the audio
     */
    suspend fun deleteImages(audioFile: AudioFile) {
        val file = audioFile.image
        if (file != null)
            fileService.deleteFile(file)

        val fileDe = audioFile.imageDe
        if (fileDe != null)
            fileService.deleteFile(fileDe)

        audioFile.image = null
        audioFile.imageDe = null
        audioFileRepo.save(audioFile)
    }

    private fun checkExtensionAudio(fileName: String): String {
        // TODO: check for specific extensions

        val lastDotIndex = fileName.indexOfLast { c -> c == '.' }
        if (lastDotIndex == -1)
            throw ErrorCode(400, "Extension not found in $fileName")

        return fileName.substring(lastDotIndex + 1).lowercase()
    }

    private fun checkExtensionImage(fileName: String): String {
        val lowerCase = fileName.lowercase()
        return when {
            lowerCase.endsWith(".jpg") || lowerCase.endsWith(".jpeg") -> "jpg"
            lowerCase.endsWith(".png") -> "png"
            else -> throw ErrorCode(409, "file type not allowed")
        }
    }
}