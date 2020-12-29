package de.ovgu.ikus.service

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.model.*
import de.ovgu.ikus.repository.AudioFileRepo
import kotlinx.coroutines.flow.collect
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service

@Service
class AudioFileService(
    private val audioFileRepo: AudioFileRepo,
    private val fileService: FileService
) {

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
        fileService.deleteFile(file.file)
        fileService.deleteFile(file.fileDe)
        audioFileRepo.delete(file)
    }

    suspend fun findMaxPosition(): Int {
        return audioFileRepo.findMaxPosition() ?: -1
    }

    /**
     * stores the audio file to the hard drive and updates the file attribute of the audio file
     */
    suspend fun setAudio(audioFile: AudioFile, filePart: FilePart, locale: IkusLocale) {
        checkExtension(filePart.filename())

        val path = "audio/files/${audioFile.id}-$locale.mp3"
        fileService.storeFilePart(filePart, path)

        when (locale) {
            IkusLocale.EN -> audioFile.file = path
            IkusLocale.DE -> audioFile.fileDe = path
        }

        audioFileRepo.save(audioFile)
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