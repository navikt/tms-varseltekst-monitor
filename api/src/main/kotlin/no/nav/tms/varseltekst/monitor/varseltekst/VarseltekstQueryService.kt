package no.nav.tms.varseltekst.monitor.varseltekst

import kotlinx.coroutines.*
import org.apache.poi.ss.usermodel.Workbook
import java.time.LocalDate
import java.util.*

class VarseltekstQueryService(
    private val varseltekstRepository: VarseltekstRepository
) {
    private val fileStore = mutableMapOf<String, ExcelFile>()

    private val queryScope = CoroutineScope(Dispatchers.IO + Job())

    fun processRequestAsync(request: DownloadRequest): String {

        val fileId = UUID.randomUUID().toString()
        val filename = filename(request)

        fileStore[fileId] = ExcelFile.waiting(filename)

        queryScope.launch {
            processRequest(request).let { workbook ->
                fileStore[fileId] = ExcelFile.ready(filename, workbook)
            }
        }

        return fileId
    }

    fun fileStatus(fileId: String): FileStatus {
        val excelFile = fileStore[fileId]

        return when (excelFile?.isReady) {
            null -> FileStatus.NotAvailable
            false -> FileStatus.Pending
            true -> FileStatus.Complete
        }
    }

    fun releaseFile(fileId: String): ExcelFile {
        val excelFile = fileStore[fileId] ?: throw FileNotFoundException(fileId)

        if (!excelFile.isReady) {
            throw FileNotReadyException(fileId)
        }

        fileStore.remove(fileId)

        return excelFile
    }

    private fun processRequest(request: DownloadRequest): Workbook {
        return if (request.detaljert) {
            varseltekstRepository.tellAntallVarseltekster(
                teksttyper = request.teksttyper,
                varseltype = request.varseltype,
                startDato = request.startDato,
                sluttDato = request.sluttDato,
                inkluderStandardtekster = request.inkluderStandardtekster
            ).let {
                sladdDetaljerteTekster(it, request.minimumAntall)
            }.let {
                ExcelWorkbookWriter.antallToExcelSheet(it)
            }
        } else {
            varseltekstRepository.tellAntallVarselteksterTotalt(
                teksttyper = request.teksttyper,
                varseltype = request.varseltype,
                startDato = request.startDato,
                sluttDato = request.sluttDato,
                inkluderStandardtekster = request.inkluderStandardtekster
            ).let {
                sladdTekster(it, request.minimumAntall)
            }.let {
                ExcelWorkbookWriter.totaltAntallToExcelSheet(it)
            }
        }
    }

    private fun sladdTekster(totaltAntall: TotaltAntall, minAntall: Int): TotaltAntall {

        val (beholdes, sladdes) = totaltAntall.permutasjoner.partition {
            permutasjon -> permutasjon.antall >= minAntall
                || permutasjon.tekster.none { it.innhold == Tekst.Innhold.Egendefinert }
        }

        val sladdet = sladdes.map { permutasjon ->
            TotaltAntall.Permutasjon(
                antall = permutasjon.antall,
                tekster = permutasjon.tekster.map { tekst ->
                    if (tekst.innhold == Tekst.Innhold.Egendefinert) {
                        Tekst(null, Tekst.Innhold.Sladdet)
                    } else {
                        tekst
                    }
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


    private fun sladdDetaljerteTekster(detaljertAntall: DetaljertAntall, minAntall: Int): DetaljertAntall {

        val (beholdes, sladdes) = detaljertAntall.permutasjoner.partition { permutasjon ->
            permutasjon.antall >= minAntall
                || permutasjon.tekster.none {it.innhold == Tekst.Innhold.Egendefinert }
        }

        val sladdet = sladdes.map { permutasjon ->
            DetaljertAntall.Permutasjon(
                varseltype = permutasjon.varseltype,
                produsent = permutasjon.produsent,
                antall = permutasjon.antall,
                tekster = permutasjon.tekster.map { tekst ->
                    if (tekst.innhold == Tekst.Innhold.Egendefinert) {
                        Tekst(null, Tekst.Innhold.Sladdet)
                    } else {
                        tekst
                    }
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

    data class ExcelFile(
        val filename: String,
        var workbook: Workbook?,
    ) {
        val isReady: Boolean get() = workbook != null

        companion object {

            fun waiting(filename: String) = ExcelFile(
                filename = filename,
                workbook = null,
            )

            fun ready(filename: String, workbook: Workbook) = ExcelFile(
                filename = filename,
                workbook = workbook
            )
        }
    }

    private fun filename(request: DownloadRequest): String {
        return when {
            request.filnavn == null -> {
                val teksttypePart = if (request.teksttyper.size == 1) {
                    request.teksttyper.first().name.lowercase()
                } else {
                    "kombinasjon"
                }

                "${LocalDate.now()}-varseltekster-$teksttypePart-${if (request.detaljert) "" else "totalt-"}antall.xlsx"
            }
            request.filnavn.endsWith(".xlsx") -> request.filnavn
            else -> "${request.filnavn}.xlsx"
        }
    }
}

class FileNotFoundException(val fileId: String): IllegalArgumentException()
class FileNotReadyException(val fileId: String): IllegalArgumentException()
