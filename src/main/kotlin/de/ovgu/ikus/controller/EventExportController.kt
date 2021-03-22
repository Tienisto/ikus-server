package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.model.RegistrationField
import de.ovgu.ikus.service.EventRegistrationExportService
import de.ovgu.ikus.service.EventService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/api/events/export")
class EventExportController(
    private val eventService: EventService,
    private val eventRegistrationExportService: EventRegistrationExportService
) {

    @RequestMapping("/registrations")
    suspend fun exportRegistrations(@RequestParam eventId: Int, @RequestParam(required = false) fields: List<RegistrationField>?): ResponseEntity<*> {
        val event = eventService.findById(eventId) ?: throw ErrorCode(404, "Event not found")
        val (title, export) = eventRegistrationExportService.exportPDF(event, fields ?: listOf(RegistrationField.FIRST_NAME, RegistrationField.LAST_NAME))
        return ResponseEntity.ok()
            .contentLength(export.size.toLong())
            .contentType(MediaType.parseMediaType("application/pdf"))
            .header("Content-Disposition", "inline; filename=\"$title.pdf\"")
            .body(export)
    }
}