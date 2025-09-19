package no.nav.tms.varseltekst.monitor.varseltekst

import no.nav.tms.varseltekst.monitor.varseltekst.Tekst.Innhold.*
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook


object ExcelWorkbookWriter {

    fun totaltAntallToExcelSheet(totaltAntall: TotaltAntall): Workbook {

        val (workbook, sheet) = initWorkbook("Antall", teksttyper = totaltAntall.teksttyper)

        totaltAntall.teksttyper.forEachIndexed { index, _ ->
            sheet.setColumnWidth(1 + index, 25000)
        }

        totaltAntall.permutasjoner.forEachIndexed { i, permutasjon ->
            val row = sheet.createRow(i + 1)

            row.createCell(0).apply {
                cellType = CellType.NUMERIC
                setCellValue(permutasjon.antall.toDouble())
            }

            permutasjon.tekster.forEachIndexed { index, tekst ->
                row.createCell(1 + index).apply {
                    cellType = CellType.STRING
                    val displayText = when (tekst.innhold) {
                        Egendefinert -> tekst.tekst!!
                        Sladdet -> "<sladdet>"
                        Standard -> "<standardtekst>"
                        Ingen -> "<ingen>"
                    }
                    setCellValue(displayText)
                }
            }
        }

        return workbook
    }

    fun antallToExcelSheet(detaljertAntall: DetaljertAntall): Workbook {

        val (workbook, sheet) = initWorkbook("Antall", "Varseltype", "Namespace", "Appnavn", teksttyper = detaljertAntall.teksttyper)

        sheet.setColumnWidth(2, 3000)
        sheet.setColumnWidth(3, 5000)

        detaljertAntall.teksttyper.forEachIndexed { index, _ ->
            sheet.setColumnWidth(4 + index, 25000)
        }

        detaljertAntall.permutasjoner.forEachIndexed { i, permutasjon ->
            val row = sheet.createRow(i + 1)

            row.createCell(0).apply {
                cellType = CellType.NUMERIC
                setCellValue(permutasjon.antall.toDouble())
            }

            row.createCell(1).apply {
                cellType = CellType.STRING
                setCellValue(permutasjon.varseltype)
            }

            row.createCell(2).apply {
                cellType = CellType.STRING
                setCellValue(permutasjon.produsent.namespace)
            }

            row.createCell(3).apply {
                cellType = CellType.STRING
                setCellValue(permutasjon.produsent.appnavn)
            }

            permutasjon.tekster.forEachIndexed { index, tekst ->
                row.createCell(4 + index).apply {
                    cellType = CellType.STRING
                    val displayText = when (tekst.innhold) {
                        Egendefinert -> tekst.tekst!!
                        Sladdet -> "<sladdet>"
                        Standard -> "<standardtekst>"
                        Ingen -> "<ingen>"
                    }
                    setCellValue(displayText)
                }
            }
        }

        return workbook
    }

    private fun initWorkbook(vararg columns: String, teksttyper: List<Teksttype>): Pair<XSSFWorkbook, XSSFSheet> {
        val workbook = XSSFWorkbook()

        val sheetName = if (teksttyper.size == 1) {
            teksttyper.first().name
        } else {
            "Kombinasjon"
        }

        val sheet = workbook.createSheet(sheetName)

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

        teksttyper.forEachIndexed { i, teksttype ->
            header.createCell(i + columns.size).apply {
                cellStyle = headerStyle
                setCellValue(teksttype.name)
            }
        }

        return workbook to sheet
    }
}
