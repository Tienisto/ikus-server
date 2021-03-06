package de.ovgu.ikus.service

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.model.*
import de.ovgu.ikus.repository.ContactRepo
import kotlinx.coroutines.flow.collect
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service

@Service
class ContactService(
        private val contactRepo: ContactRepo,
        private val fileService: FileService,
        private val imageService: ImageService
) {

    suspend fun findAllOrdered(): List<Contact> {
        return contactRepo.findByOrderByPosition()
    }

    suspend fun findById(id: Int): Contact? {
        return contactRepo.findById(id)
    }

    suspend fun save(contact: Contact): Contact {
        return contactRepo.save(contact)
    }

    suspend fun saveAll(files: List<Contact>) {
        contactRepo.saveAll(files).collect()
    }

    /**
     * delete the contact and also deletes the image if it exists
     */
    suspend fun delete(contact: Contact) {
        deleteFile(contact)
        contactRepo.delete(contact)
    }

    suspend fun deleteAll() {
        contactRepo.deleteAll()
    }

    /**
     * stores the image to the hard drive and updates the file attribute of the contact
     */
    suspend fun setFile(contact: Contact, file: FilePart) {
        checkExtension(file.filename())

        val inputStream = imageService.digestImage(file)
        val path = "contacts/${contact.id}.jpg"
        fileService.storeFileInputStream(inputStream, path)
        contact.file = path
        contactRepo.save(contact)
    }

    /**
     * deletes the image if existing, updates the contact
     */
    suspend fun deleteFile(contact: Contact) {
        val file = contact.file ?: return
        fileService.deleteFile(file)
        contact.file = null
        contactRepo.save(contact)
    }

    private fun checkExtension(fileName: String): String {
        val lowerCase = fileName.lowercase()
        return when {
            lowerCase.endsWith(".jpg") || lowerCase.endsWith(".jpeg") -> "jpg"
            lowerCase.endsWith(".png") -> "png"
            else -> throw ErrorCode(409, "file type not allowed")
        }
    }

    suspend fun findMaxPosition(): Int {
        return contactRepo.findMaxPosition() ?: -1
    }
}