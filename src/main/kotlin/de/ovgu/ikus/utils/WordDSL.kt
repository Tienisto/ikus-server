package de.ovgu.ikus.utils

import org.apache.poi.util.Units
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr
import org.springframework.core.io.ClassPathResource
import java.io.ByteArrayOutputStream
import java.math.BigInteger

fun wordDocument(content: XWPFDocument.() -> Unit): ByteArray {
    // init
    val document = XWPFDocument()

    // set small padding https://stackoverflow.com/a/66209694/11127438
    var sectPr: CTSectPr? = document.document.body.sectPr
    if (sectPr == null) sectPr = document.document.body.addNewSectPr()
    var pageMar = sectPr!!.pgMar
    if (pageMar == null) pageMar = sectPr.addNewPgMar()
    pageMar!!.left = BigInteger.valueOf(720)
    pageMar.right = BigInteger.valueOf(720)
    pageMar.top = BigInteger.valueOf(720)
    pageMar.bottom = BigInteger.valueOf(720)
    pageMar.footer = BigInteger.valueOf(720)
    pageMar.header = BigInteger.valueOf(720)
    pageMar.gutter = BigInteger.valueOf(0)

    // content
    content(document)

    // write
    val outputStream = ByteArrayOutputStream()
    document.write(outputStream)
    document.close()
    return outputStream.toByteArray()
}

fun XWPFDocument.text(text: String, fontSize: Int = 12, bold: Boolean = false, color: String = "000000", spacingAfter: Int = 0) {
    val paragraph = createParagraph()
    paragraph.spacingAfter = spacingAfter
    val run = paragraph.createRun()
    run.setText(text)
    run.isBold = bold
    run.fontSize = fontSize
    run.color = color
}

fun XWPFDocument.logo() {
    val paragraph = createParagraph()
    val run = paragraph.createRun()

    val path = "ovgu-banner.png"
    val width = 355.0
    val height = 128.0
    run.addPicture(
        ClassPathResource(path).inputStream,
        XWPFDocument.PICTURE_TYPE_PNG,
        path,
        Units.toEMU(width * 0.3),
        Units.toEMU(height * 0.3)
    )
}

fun XWPFDocument.table(header: List<String>, rows: List<List<String>>, rowWidths: List<Long>) {
    val table = createTable(rows.size + 1, header.size)
    table.setWidth("100%")

    val headerRow = table.getRow(0)
    header.forEachIndexed { index, text ->
        val cell = headerRow.getCell(index)
        cell.text = text
        cell.paragraphs[0].runs[0].isBold = true
        cell.paragraphs[0].alignment = ParagraphAlignment.CENTER

//        cell.widthType = TableWidthType.PCT
//        cell.setWidth(rowWidths[index].toString())
    }

    rows.forEachIndexed { rowIndex, rowContent ->
        val currentRow = table.getRow(rowIndex + 1)

        rowContent.forEachIndexed { colIndex, text ->
            val cell = currentRow.getCell(colIndex)
            cell.text = text
//            cell.widthType = TableWidthType.PCT
//            cell.setWidth(rowWidths[colIndex].toString())
            cell.paragraphs[0].alignment = ParagraphAlignment.CENTER
        }
    }
}
