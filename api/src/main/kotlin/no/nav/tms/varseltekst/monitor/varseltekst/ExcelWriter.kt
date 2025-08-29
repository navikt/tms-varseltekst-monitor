package no.nav.tms.varseltekst.monitor.varseltekst

import no.nav.tms.varseltekst.monitor.varseltekst.VarselTekster.DetaljertAntall
import no.nav.tms.varseltekst.monitor.varseltekst.VarselTekster.TotaltAntall
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook


object ExcelWriter {
    fun totaltAntallToExcelSheet(teksttype: Teksttype, totaltAntall: List<TotaltAntall>, minAntall: Int): Workbook {
        val (workbook, sheet) = initWorkbook(teksttype, "Antall", "Tekst")

        sheet.setColumnWidth(1, 25000)

        val antallTekster = sladdTekster(totaltAntall, minAntall)

        antallTekster.forEachIndexed { i, antall ->
            val row = sheet.createRow(i + 1)

            row.createCell(0).apply {
                cellType = CellType.NUMERIC
                setCellValue(antall.antall.toDouble())
            }

            row.createCell(1).apply {
                cellType = CellType.STRING
                setCellValue(antall.tekst ?: "<standardtekst>")
            }
        }

        return workbook
    }

    fun antallToExcelSheet(teksttype: Teksttype, detaljertAntall: List<DetaljertAntall>, minAntall: Int): Workbook {
        val (workbook, sheet) = initWorkbook(teksttype, "Antall", "Varseltype", "Namespace", "Appnavn", "Tekst")

        sheet.setColumnWidth(2, 3000)
        sheet.setColumnWidth(3, 5000)
        sheet.setColumnWidth(4, 25000)

        val antallTekster = sladdDetaljerteTekster(detaljertAntall, minAntall)

        antallTekster.forEachIndexed { i, antall ->
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
                setCellValue(antall.produsent.namespace)
            }

            row.createCell(3).apply {
                cellType = CellType.STRING
                setCellValue(antall.produsent.appnavn)
            }

            row.createCell(4).apply {
                cellType = CellType.STRING
                setCellValue(antall.tekst ?: "<standardtekst>")
            }
        }

        return workbook
    }

    private fun sladdDetaljerteTekster(tekster: List<DetaljertAntall>, minAntall: Int): List<DetaljertAntall> {
        val (beholdes, skalSladdes) = tekster.partition { it.antall >= minAntall || it.tekst == null }

        val sladdet = skalSladdes
            .groupBy { it.varseltype to it.produsent }
            .mapValues { (_, v) -> v.sumOf { it.antall } }
            .map { (typeProdusent, sumAntall) ->

                val (varseltype, produsent) = typeProdusent

                DetaljertAntall(
                    varseltype = varseltype,
                    produsent = produsent,
                    antall = sumAntall,
                    tekst = "<sladdet>"
                )
            }

        return (beholdes + sladdet).sortedByDescending { it.antall }
    }

    private fun sladdTekster(tekster: List<TotaltAntall>, minAntall: Int): List<TotaltAntall> {
        val (beholdes, sladdes) = tekster.partition { it.antall >= minAntall || it.tekst == null }

        val sumAntall = sladdes.sumOf { it.antall }

        val sladdet = TotaltAntall(
            antall = sumAntall,
            tekst = "<sladdet>"
        )

        return (beholdes + sladdet).sortedByDescending { it.antall }
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
