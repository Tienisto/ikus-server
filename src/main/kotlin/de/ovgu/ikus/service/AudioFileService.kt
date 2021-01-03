package de.ovgu.ikus.service

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.model.*
import de.ovgu.ikus.repository.AudioFileRepo
import kotlinx.coroutines.flow.collect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream

@Service
class AudioFileService(
    private val audioFileRepo: AudioFileRepo,
    private val fileService: FileService
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
            fileService.deleteFile(file.file)
            fileService.deleteFile(file.fileDe)
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

        val extension = checkExtension(fileName)
        val path = "audio/files/${audioFile.id}-$locale.$extension"
        fileService.storeFileInputStream(ByteArrayInputStream(byteArray), path)

        when (locale) {
            IkusLocale.EN -> audioFile.file = path
            IkusLocale.DE -> audioFile.fileDe = path
        }

        audioFileRepo.save(audioFile)
    }

    private fun checkExtension(fileName: String): String {
        // TODO: check for specific extensions

        val lastDotIndex = fileName.indexOfLast { c -> c == '.' }
        if (lastDotIndex == -1)
            throw ErrorCode(400, "Extension not found in $fileName")

        return fileName.substring(lastDotIndex + 1).toLowerCase()
    }
}