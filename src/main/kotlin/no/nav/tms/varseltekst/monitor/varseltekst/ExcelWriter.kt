package no.nav.tms.varseltekst.monitor.varseltekst

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook


object ExcelWriter {
    fun totaltAntallToExcelSheet(teksttype: Teksttype, totaltAntall: List<VarselTekster.TotaltAntall>): Workbook {
        val (workbook, sheet) = initWorkbook(teksttype, "Antall", "Tekst")

        sheet.setColumnWidth(1, 100)

        totaltAntall.forEachIndexed { i, antall ->
            val row = sheet.createRow(i + 1)

            row.createCell(0).apply {
                cellType = CellType.NUMERIC
                setCellValue(antall.antall.toDouble())
            }

            row.createCell(1).apply {
                cellType = CellType.STRING
                setCellValue(antall.tekst)
            }
        }

        return workbook
    }

    fun antallToExcelSheet(teksttype: Teksttype, totaltAntall: List<VarselTekster.DetaljertAntall>): Workbook {
        val (workbook, sheet) = initWorkbook(teksttype, "Antall", "Varseltype", "Namespace", "Appnavn", "Tekst")

        sheet.setColumnWidth(2, 15)
        sheet.setColumnWidth(3, 25)
        sheet.setColumnWidth(4, 100)

        totaltAntall.forEachIndexed { i, antall ->
            val row = sheet.createRow(i + 1)

            row.createCell(0).apply {
                cellType = CellType.NUMERIC
                setCellValue(antall.antall.toDouble())
            }

            row.createCell(1).apply {
                cellType = CellType.STRING
                setCellValue(antall.varseltype)
            }

            row.createCell(2).apply {
                cellType = CellType.STRING
                setCellValue(antall.produsentNamespace)
            }

            row.createCell(3).apply {
                cellType = CellType.STRING
                setCellValue(antall.produsentAppnavn)
            }

            row.createCell(4).apply {
                cellType = CellType.STRING
                setCellValue(antall.tekst)
            }
        }

        return workbook
    }

    private fun initWorkbook(teksttype: Teksttype, vararg columns: String): Pair<XSSFWorkbook, XSSFSheet> {
        val workbook = XSSFWorkbook()

        val sheet = workbook.createSheet(teksttype.name)

        val header = sheet.createRow(0)

        val headerStyle = workbook.createCellStyle().also { style ->
            workbook.createFont().also {
                it.bold = true
            }.let {
                style.setFont(it)
            }
        }

        columns.forEachIndexed { i, column ->
            header.createCell(i).apply {
                cellStyle = headerStyle
                setCellValue(column)
            }
        }

        return workbook to sheet
    }
}
