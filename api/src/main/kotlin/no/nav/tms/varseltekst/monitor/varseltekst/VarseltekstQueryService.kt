package no.nav.tms.varseltekst.monitor.varseltekst

import io.ktor.server.response.*
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
            if (request.detaljert) {
                varseltekstRepository.tellAntallVarseltekster(
                    teksttyper = request.teksttyper,
                    varseltype = request.varseltype,
                    startDato = request.startDato,
                    sluttDato = request.sluttDato,
                    inkluderStandardtekster = request.inkluderStandardtekster
                ).let {
                    ExcelFileWriter.antallToExcelSheet(it, request.teksttyper, request.minimumAntall)
                }
            } else {
                varseltekstRepository.tellAntallVarselteksterTotalt(
                    teksttyper = request.teksttyper,
                    varseltype = request.varseltype,
                    startDato = request.startDato,
                    sluttDato = request.sluttDato,
                    inkluderStandardtekster = request.inkluderStandardtekster
                ).let {
                    ExcelFileWriter.totaltAntallToExcelSheet(it, request.teksttyper, request.minimumAntall)
                }
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
