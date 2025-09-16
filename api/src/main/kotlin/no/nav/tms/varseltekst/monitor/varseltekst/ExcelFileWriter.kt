package no.nav.tms.varseltekst.monitor.varseltekst

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook


object ExcelFileWriter {

    fun totaltAntallToExcelSheet(totaltAntall: TotaltAntall, teksttype: Teksttype, minAntall: Int): Workbook {

        val tekstkolonner = totaltAntall.teksttyper.map { it.name }.toTypedArray()

        val (workbook, sheet) = initWorkbook(teksttype, "Antall", *tekstkolonner)

        sheet.setColumnWidth(1, 25000)

        val antallTekster = sladdTekster(totaltAntall, minAntall)

        antallTekster.permutasjoner.forEachIndexed { i, permutasjon ->
            val row = sheet.createRow(i + 1)

            row.createCell(0).apply {
                cellType = CellType.NUMERIC
                setCellValue(permutasjon.antall.toDouble())
            }

            permutasjon.tekster.forEachIndexed { index, tekst ->
                row.createCell(1 + index).apply {
                    cellType = CellType.STRING
                    setCellValue(tekst.tekst ?: "<standardtekst>")
                }
            }
        }

        return workbook
    }

    fun antallToExcelSheet(detaljertAntall: DetaljertAntall, teksttype: Teksttype, minAntall: Int): Workbook {

        val tekstkolonner = detaljertAntall.teksttyper.map { it.name }.toTypedArray()

        val (workbook, sheet) = initWorkbook(teksttype, "Antall", "Varseltype", "Namespace", "Appnavn", *tekstkolonner)

        sheet.setColumnWidth(2, 3000)
        sheet.setColumnWidth(3, 5000)
        sheet.setColumnWidth(4, 25000)

        val antallTekster = sladdDetaljerteTekster(detaljertAntall, minAntall)

        antallTekster.permutasjoner.forEachIndexed { i, permutasjon ->
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
                    setCellValue(tekst.tekst ?: "<standardtekst>")
                }
            }
        }

        return workbook
    }

    private fun sladdDetaljerteTekster(detaljertAntall: DetaljertAntall, minAntall: Int): DetaljertAntall {

        val (beholdes, sladdes) = detaljertAntall.permutasjoner.partition { it.antall >= minAntall || it.tekster.all(Tekst::isStandard) }

        val sladdet = sladdes.map { permutasjon ->
            DetaljertAntall.Permutasjon(
                varseltype = permutasjon.varseltype,
                produsent = permutasjon.produsent,
                antall = permutasjon.antall,
                tekster = permutasjon.tekster.map {
                    Tekst(if (it.isStandard) null else "<sladdet>")
                }
            )
        }.groupBy {
            Triple(it.varseltype, it.produsent, it.tekster)
        }.map { (vpt, permutasjoner) ->
            DetaljertAntall.Permutasjon(
                varseltype = vpt.first,
                produsent = vpt.second,
                antall = permutasjoner.sumOf { it.antall },
                tekster = vpt.third
            )
        }


        return (sladdet + beholdes)
            .sortedByDescending { it.antall }
            .let { DetaljertAntall(detaljertAntall.teksttyper, it) }
    }

    private fun sladdTekster(totaltAntall: TotaltAntall, minAntall: Int): TotaltAntall {

        val (beholdes, sladdes) = totaltAntall.permutasjoner.partition { it.antall >= minAntall || it.tekster.all(Tekst::isStandard) }

        val sladdet = sladdes.map { permutasjon ->
            TotaltAntall.Permutasjon(
                antall = permutasjon.antall,
                tekster = permutasjon.tekster.map {
                    Tekst(if (it.isStandard) null else "<sladdet>")
                }
            )
        }.groupBy {
            it.tekster
        }.map { (tekster, permutasjoner) ->
            TotaltAntall.Permutasjon(
                antall = permutasjoner.sumOf { it.antall },
                tekster = tekster
            )
        }

        return (sladdet + beholdes)
            .sortedByDescending { it.antall }
            .let { TotaltAntall(totaltAntall.teksttyper, it) }
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
