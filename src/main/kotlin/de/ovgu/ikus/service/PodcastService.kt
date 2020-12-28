package de.ovgu.ikus.service

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.model.Podcast
import de.ovgu.ikus.repository.PodcastRepo
import kotlinx.coroutines.flow.collect
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service

@Service
class PodcastService (
    private val podcastRepo: PodcastRepo,
    private val imageService: ImageService,
    private val fileService: FileService
) {

    suspend fun findAllOrdered(): List<Podcast> {
        return podcastRepo.findByOrderByPosition()
    }

    suspend fun findById(id: Int): Podcast? {
        return podcastRepo.findById(id)
    }

    suspend fun save(contact: Podcast): Podcast {
        return podcastRepo.save(contact)
    }

    suspend fun saveAll(files: List<Podcast>) {
        podcastRepo.saveAll(files).collect()
    }

    suspend fun delete(podcast: Podcast) {
        deleteImages(podcast)
        podcastRepo.delete(podcast)
    }

    suspend fun deleteAll() {
        podcastRepo.deleteAll()
    }

    /**
     * stores the image to the hard drive and updates the file attribute of the podcast
     */
    suspend fun setImage(podcast: Podcast, file: FilePart, locale: IkusLocale) {
        checkExtension(file.filename())

        val path = "podcasts/${podcast.id}-$locale.jpg"
        val inputStream = imageService.digestImage(file)
        fileService.storeFileInputStream(inputStream, path)

        when (locale) {
            IkusLocale.EN -> podcast.image = path
            IkusLocale.DE -> podcast.imageDe = path
        }

        podcastRepo.save(podcast)
    }

    /**
     * deletes the images if existing, updates the podcast
     */
    suspend fun deleteImages(podcast: Podcast) {
        val file = podcast.image
        if (file != null)
            fileService.deleteFile(file)

        val fileDe = podcast.imageDe
        if (fileDe != null)
            fileService.deleteFile(fileDe)

        podcast.image = null
        podcast.imageDe = null
        podcastRepo.save(podcast)
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
        return podcastRepo.findMaxPosition() ?: -1
    }
}