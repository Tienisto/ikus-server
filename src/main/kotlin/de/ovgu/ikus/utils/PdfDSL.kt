package de.ovgu.ikus.utils

import com.lowagie.text.*
import com.lowagie.text.pdf.*
import de.ovgu.ikus.dto.ErrorCode
import org.springframework.core.io.ClassPathResource
import java.awt.Color
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

private val smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10F)
private val cellFont = FontFactory.getFont(FontFactory.HELVETICA, 12F)
private val spacer = Chunk("\n")

fun document(title: String, content: Document.() -> Unit): ByteArray {
    // init
    val document = Document()
    val outputStream = ByteArrayOutputStream()
    val writer = PdfWriter.getInstance(document, outputStream)

    //title
    writer.pageEvent = PageNumeration(title)
    document.addTitle(title)

    // content
    document.open()
    content(document)
    document.close()

    return outputStream.toByteArray()
}

fun Document.text(text: String, font: Font = cellFont, spacingBefore: Float = 5F, spacingAfter: Float = 5F) {
    val p = Paragraph(text, font)
    p.spacingBefore = spacingBefore
    p.spacingAfter = spacingAfter
    add(p)
}

fun Document.spacer() {
    add(spacer)
}

fun Document.table(columns: Int, content: PdfPTable.() -> Unit) {
    table(PdfPTable(columns), content)
}

fun Document.table(columns: List<Float>, content: PdfPTable.() -> Unit) {
    table(PdfPTable(columns.toFloatArray()), content)
}

private fun Document.table(table: PdfPTable, content: PdfPTable.() -> Unit) {
    table.widthPercentage = 100F
    content(table)
    add(table)
}

fun PdfPTable.cell(text: String? = null,
                   image: Image? = null,
                   font: Font = cellFont,
                   background: Color? = null,
                   horizontalAlignment: Int = Element.ALIGN_CENTER,
                   verticalAlignment: Int = Element.ALIGN_MIDDLE,
                   rotation: Int = 0,
                   paddingTop: Float = 5F, paddingRight: Float = 0F, paddingBottom: Float = 5F, paddingLeft: Float = 0F,
                   borderWidth: Float = 1F,
                   borderTop: Float? = null,
                   borderRight: Float? = null,
                   borderBottom: Float? = null,
                   borderLeft: Float? = null,
                   properties: PdfPCell.() -> Unit = {}) {

    val cell = when {
        text != null -> PdfPCell(Phrase(text, font))
        image != null -> PdfPCell(image)
        else -> throw ErrorCode(500, "invalid cell arguments")
    }
    cell.backgroundColor = background
    cell.isUseAscender = true
    cell.horizontalAlignment = horizontalAlignment
    cell.verticalAlignment = verticalAlignment
    cell.rotation = rotation
    cell.paddingTop = paddingTop
    cell.paddingRight = paddingRight
    cell.paddingBottom = paddingBottom
    cell.paddingLeft = paddingLeft
    cell.borderWidth = borderWidth
    borderTop?.let { cell.borderWidthTop = it }
    borderRight?.let { cell.borderWidthRight = it }
    borderBottom?.let { cell.borderWidthBottom = it }
    borderLeft?.let { cell.borderWidthLeft = it }
    properties(cell)
    addCell(cell)
}

private class PageNumeration(val title: String) : PdfPageEventHelper() {

    var total: PdfTemplate? = null
    val logo = Image
        .getInstance(ImageIO.read(ClassPathResource("ovgu-banner.png").inputStream), null)
        .apply { scaleAbsolute(111F, 40F) }

    override fun onOpenDocument(writer: PdfWriter, document: Document) {
        total = writer.directContent.createTemplate(30F, 12F)
    }

    override fun onStartPage(writer: PdfWriter, document: Document) {
        document.apply {
            if(writer.pageNumber == 1) {
                add(logo)
            } else {
                table(2) {
                    headerRows = 0
                    cell(image = logo, paddingBottom = 20F, horizontalAlignment = Element.ALIGN_LEFT, borderWidth = 0F)
                    cell(title, font = smallFont, horizontalAlignment = Element.ALIGN_RIGHT, borderWidth = 0F)
                }
            }
        }
    }

    override fun onEndPage(writer: PdfWriter, document: Document) {
        val table = PdfPTable(floatArrayOf(30F, 10F, 1F))
        table.apply {
            cell(title, font = smallFont, horizontalAlignment = Element.ALIGN_LEFT, verticalAlignment = Element.ALIGN_BOTTOM, borderWidth = 0F)
            cell("Seite ${writer.pageNumber} / ", font = smallFont, horizontalAlignment = Element.ALIGN_RIGHT, verticalAlignment = Element.ALIGN_BOTTOM, paddingRight = 2F, borderWidth = 0F)
            cell(image = Image.getInstance(total), borderWidth = 0F)
            totalWidth = (document.pageSize.width - document.leftMargin() - document.rightMargin())
            writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin(), writer.directContent)
        }
    }

    override fun onCloseDocument(writer: PdfWriter, document: Document) {
        ColumnText.showTextAligned(total, Element.ALIGN_LEFT, Phrase("${(writer.pageNumber - 1)}", smallFont), 0F, 0F, 0F)
    }
}