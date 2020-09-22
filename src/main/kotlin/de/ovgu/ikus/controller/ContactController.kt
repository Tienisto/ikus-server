package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.ContactDto
import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.dto.Request
import de.ovgu.ikus.model.Contact
import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.model.LogType
import de.ovgu.ikus.security.toUser
import de.ovgu.ikus.service.CacheKey
import de.ovgu.ikus.service.CacheService
import de.ovgu.ikus.service.ContactService
import de.ovgu.ikus.service.LogService
import de.ovgu.ikus.utils.toDto
import de.ovgu.ikus.utils.trimOrNull
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/contacts")
class ContactController (
        private val logService: LogService,
        private val cacheService: CacheService,
        private val contactService: ContactService
) {

    @GetMapping
    suspend fun getContacts(): List<ContactDto> {
        return contactService
                .findAllOrdered(IkusLocale.EN)
                .map { contact -> contact.toDto() }
    }

    @PostMapping
    suspend fun createContact(authentication: Authentication, @RequestBody request: Request.CreateContact): ContactDto {
        val contact = Contact(
                email = request.email?.trimOrNull(), phoneNumber = request.phoneNumber?.trimOrNull(), place = request.place?.trimOrNull(),
                name = request.name.en.trim(), nameDe = request.name.de.trim(),
                openingHours = request.openingHours?.en?.trimOrNull(), openingHoursDe = request.openingHours?.de?.trimOrNull()
        )

        val savedContact = contactService.save(contact)
        logService.log(LogType.CREATE_CONTACT, authentication.toUser(), "${contact.name} (${contact.nameDe})")
        cacheService.triggerUpdateFlag(CacheKey.CONTACTS)

        return savedContact.toDto()
    }

    @PutMapping
    suspend fun updateContact(authentication: Authentication, @RequestBody request: Request.UpdateContact) {
        val contact = contactService.findById(request.id) ?: throw ErrorCode(404, "Contact not found")

        // apply
        contact.email = request.email?.trimOrNull()
        contact.phoneNumber = request.phoneNumber?.trimOrNull()
        contact.place = request.place?.trimOrNull()
        contact.name = request.name.en.trim()
        contact.nameDe = request.name.de.trim()
        contact.openingHours = request.openingHours?.en?.trimOrNull()
        contact.openingHoursDe = request.openingHours?.de?.trimOrNull()

        contactService.save(contact)
        logService.log(LogType.UPDATE_CONTACT, authentication.toUser(), "${contact.name} (${contact.nameDe})")
        cacheService.triggerUpdateFlag(CacheKey.CONTACTS)
    }

    @DeleteMapping
    suspend fun deletePost(authentication: Authentication, @RequestBody request: Request.Id) {
        val contact = contactService.findById(request.id) ?: throw ErrorCode(404, "Contact not found")
        contactService.delete(contact)
        logService.log(LogType.DELETE_CONTACT, authentication.toUser(), "${contact.name} (${contact.nameDe})")
        cacheService.triggerUpdateFlag(CacheKey.CONTACTS)
    }

    @PutMapping("/file", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun upload(@RequestParam contactId: Int, @RequestPart("file") file: Mono<FilePart>) {
        val contact = contactService.findById(contactId) ?: throw ErrorCode(404, "Contact not found")
        contactService.setFile(contact, file.awaitFirst())
    }

    @DeleteMapping("/file")
    suspend fun deleteFile(@RequestParam contactId: Int) {
        val contact = contactService.findById(contactId) ?: throw ErrorCode(404, "Contact not found")
        contactService.deleteFile(contact)
    }
}