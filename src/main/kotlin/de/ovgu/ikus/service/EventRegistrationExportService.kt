package de.ovgu.ikus.service

import com.lowagie.text.FontFactory
import de.ovgu.ikus.model.Event
import de.ovgu.ikus.model.RegistrationData
import de.ovgu.ikus.model.RegistrationField
import de.ovgu.ikus.utils.*
import org.springframework.stereotype.Service
import java.awt.Color
import java.time.format.DateTimeFormatter
import kotlin.math.roundToLong

@Service
class EventRegistrationExportService {

    private val headerColor = Color(0x7a003f)
    private val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16F)
    private val subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12F)
    private val cellTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10F, Color.WHITE)
    private val cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10F)
    private val emptyFieldString = "(?)"
    private val datePattern = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm 'Uhr'")

    fun exportPDF(event: Event, title: String, fields: List<RegistrationField>): ByteArray {
        return document(title) {
            text(event.nameDe, titleFont, spacingBefore = 0F, spacingAfter = 0F)
            text("Englisch: ${event.name}", subTitleFont, spacingBefore = 0F, spacingAfter = 0F)
            text("Beginn: ${event.startTime.germany().format(datePattern)}", subTitleFont, spacingBefore = 0F, spacingAfter = 0F)
            text("Ort: ${event.place ?: "keine Angabe"}", subTitleFont, spacingBefore = 0F, spacingAfter = 20F)

            table(getRowWidths(fields)) {

                // header
                headerRows = 1
                cell("Platz", font = cellTitleFont, background = headerColor)
                fields.forEach { field ->
                    cell(field.germanLabel, font = cellTitleFont, background = headerColor)
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
                            RegistrationField.DATE_OF_BIRTH -> cell(person.dateOfBirth?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: emptyFieldString, font = cellFont)
                        }
                    }
                    cell("")
                }
            }
        }
    }

    fun exportWord(event: Event, title: String, fields: List<RegistrationField>): ByteArray {
        return wordDocument {
            logo()
            text(event.nameDe, fontSize = 16, bold = true)
            text("Englisch: ${event.name}")
            text("Beginn: ${event.startTime.germany().format(datePattern)}")
            text("Ort: ${event.place ?: "keine Angabe"}", spacingAfter = 200)

            val header = mutableListOf<String>()
            header.add("Platz")
            header.addAll(fields.map { field -> field.germanLabel })
            header.add("Notizen")

            val rowWidths = getRowWidths(fields)
            val rowWidthSum = rowWidths.sum()
            val rowWidthsNormalized = rowWidths.map { width -> ((width / rowWidthSum) * 100 * 50).roundToLong() }

            val rows = event.registrations.mapIndexed { index, r ->
                val person = r.parseJSON<RegistrationData>()
                val row = mutableListOf<String>()
                row.add("${index + 1}")
                val actualColumns = fields.map { field ->
                    when (field) {
                        RegistrationField.MATRICULATION_NUMBER -> person.matriculationNumber?.toString() ?: emptyFieldString
                        RegistrationField.FIRST_NAME -> person.firstName ?: emptyFieldString
                        RegistrationField.LAST_NAME -> person.lastName ?: emptyFieldString
                        RegistrationField.EMAIL -> person.email ?: emptyFieldString
                        RegistrationField.ADDRESS -> person.address ?: emptyFieldString
                        RegistrationField.COUNTRY -> person.country ?: emptyFieldString
                        RegistrationField.DATE_OF_BIRTH -> person.dateOfBirth?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: emptyFieldString
                    }
                }
                row.addAll(actualColumns)
                row.add("")
                row
            }
            table(header, rows, rowWidthsNormalized)
        }
    }


    private fun getRowWidths(fields: List<RegistrationField>): List<Float> {
        return List(1 + fields.size + 1) { index ->
            when {
                index == 0 -> 1F
                index >= 1 && index <= fields.size -> when (fields[index - 1]) {
                    RegistrationField.MATRICULATION_NUMBER, RegistrationField.DATE_OF_BIRTH -> 1.5F
                    RegistrationField.FIRST_NAME, RegistrationField.LAST_NAME, RegistrationField.COUNTRY -> 2F
                    RegistrationField.EMAIL, RegistrationField.ADDRESS -> 3F
                }
                else -> 2F
            }
        }
    }
}
