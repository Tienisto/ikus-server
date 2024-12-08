package de.ovgu.ikus.utils

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream

fun excelDocument(widths: List<Int>, rows: List<List<String>>): ByteArray {
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet()

    widths.forEachIndexed { index, width ->
        sheet.setColumnWidth(index, width)
    }

    rows.forEachIndexed { rowIndex, row ->
        val sheetRow = sheet.createRow(rowIndex)
        row.forEachIndexed { cellIndex, cell ->
            sheetRow.createCell(cellIndex).setCellValue(cell)
        }
    }

    val out = ByteArrayOutputStream()
    workbook.write(out)
    workbook.close()
    return out.toByteArray()
}
