package de.ovgu.ikus.service

import com.lowagie.text.FontFactory
import de.ovgu.ikus.model.Event
import de.ovgu.ikus.model.RegistrationData
import de.ovgu.ikus.model.RegistrationField
import de.ovgu.ikus.utils.*
import org.springframework.stereotype.Service
import java.awt.Color
import java.time.format.DateTimeFormatter

@Service
class EventRegistrationExportService {

    private val headerColor = Color(0x7a003f)
    private val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16F)
    private val subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12F)
    private val cellTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10F, Color.WHITE)
    private val cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10F)
    private val emptyFieldString = "(?)"
    private val datePattern = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm 'Uhr'")

    fun exportPDF(event: Event, fields: List<RegistrationField>): Pair<String, ByteArray> {
        val title = event.nameDe
        val output = document(title) {
            text(event.nameDe, titleFont, spacingBefore = 0F, spacingAfter = 0F)
            text("Englisch: ${event.name}", subTitleFont, spacingBefore = 0F, spacingAfter = 0F)
            text("Beginn: ${event.startTime.format(datePattern)}", subTitleFont, spacingBefore = 0F, spacingAfter = 0F)
            text("Ort: ${event.place ?: "keine Angabe"}", subTitleFont, spacingBefore = 0F, spacingAfter = 20F)

            val columns = List(1 + fields.size + 1) { index ->
                when {
                    index == 0 -> 1F
                    index >= 1 && index <= fields.size -> when (fields[index - 1]) {
                        RegistrationField.MATRICULATION_NUMBER -> 1.5F
                        RegistrationField.FIRST_NAME, RegistrationField.LAST_NAME, RegistrationField.COUNTRY -> 2F
                        RegistrationField.EMAIL, RegistrationField.ADDRESS -> 3F
                    }
                    else -> 2F
                }
            }

            table(columns) {

                // header
                headerRows = 1
                cell("Platz", font = cellTitleFont, background = headerColor)
                fields.forEach { field ->
                    val name = when (field) {
                        RegistrationField.MATRICULATION_NUMBER -> "MNr."
                        RegistrationField.FIRST_NAME -> "Vorname"
                        RegistrationField.LAST_NAME -> "Nachname"
                        RegistrationField.EMAIL -> "E-Mail"
                        RegistrationField.ADDRESS -> "Adresse"
                        RegistrationField.COUNTRY -> "Land"
                    }
                    cell(name, font = cellTitleFont, background = headerColor)
                }
                cell("Notizen", font = cellTitleFont, background = headerColor)

                // data
                event.registrations.forEachIndexed { index, r ->
                    val person = r.parseJSON<RegistrationData>()
                    cell("${index + 1}", font = cellFont)
                    fields.forEach { field ->
                        when (field) {
                            RegistrationField.MATRICULATION_NUMBER -> cell(person.matriculationNumber?.toString() ?: emptyFieldString, font = cellFont)
                            RegistrationField.FIRST_NAME -> cell(person.firstName ?: emptyFieldString, font = cellFont)
                            RegistrationField.LAST_NAME -> cell(person.lastName ?: emptyFieldString, font = cellFont)
                            RegistrationField.EMAIL -> cell(person.email ?: emptyFieldString, font = cellFont)
                            RegistrationField.ADDRESS -> cell(person.address ?: emptyFieldString, font = cellFont)
                            RegistrationField.COUNTRY -> cell(person.country ?: emptyFieldString, font = cellFont)
                        }
                    }
                    cell("")
                }
            }
        }

        return Pair(title, output)
    }
}