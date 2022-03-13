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

enum class ExportType { PDF, WORD }

@Controller
@RequestMapping("/api/events/export")
class EventExportController(
    private val eventService: EventService,
    private val eventRegistrationExportService: EventRegistrationExportService
) {

    @RequestMapping("/registrations")
    suspend fun exportRegistrations(
        @RequestParam eventId: Int,
        @RequestParam type: ExportType,
        @RequestParam(required = false) fields: List<RegistrationField>?,
    ): ResponseEntity<*> {
        val event = eventService.findById(eventId) ?: throw ErrorCode(404, "Event not found")
        val title = event.nameDe
        val (export, mediaType, contentDisposition) = when (type) {
            ExportType.PDF -> Triple(
                first = eventRegistrationExportService.exportPDF(event, title, fields ?: listOf(RegistrationField.FIRST_NAME, RegistrationField.LAST_NAME)),
                second = "application/pdf",
                third = "inline; filename=\"$title.pdf\""
            )
            ExportType.WORD -> Triple(
                first = eventRegistrationExportService.exportWord(event, title, fields ?: listOf(RegistrationField.FIRST_NAME, RegistrationField.LAST_NAME)),
                second = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                third = "attachment; filename=\"$title.docx\""
            )
        }
        return ResponseEntity.ok()
            .contentLength(export.size.toLong())
            .contentType(MediaType.parseMediaType(mediaType))
            .header("Content-Disposition", contentDisposition)
            .body(export)
    }
}