package no.nav.tms.varseltekst.monitor.varseltekst

import com.fasterxml.jackson.annotation.JsonAlias
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import org.apache.poi.ss.usermodel.Workbook
import java.time.LocalDate
import java.util.*
import kotlin.math.max

fun Route.varseltekstRoutes(queryService: VarseltekstQueryProcessor) {

    val log = KotlinLogging.logger { }

    val fileStore = mutableMapOf<String, ExcelFile>()

    val processorScope = CoroutineScope(Dispatchers.IO + Job())

    post("/api/download") {

        val request: DownloadRequest = call.receive()

        log.info { "Starting query" }

        val fileId = UUID.randomUUID().toString()
        val filename = filename(request)

        fileStore[fileId] = ExcelFile.waiting(filename)

        processorScope.launch {
            queryService.processRequest(request).let { workbook ->
                fileStore[fileId] = ExcelFile.ready(filename, workbook)
            }
        }

        log.info { "Pointing client to future file location" }

        call.response.header(HttpHeaders.Location, "/api/download/$fileId")
        call.respond(HttpStatusCode.Accepted)
    }

    get("/api/download/{fileId}/status") {
        val excelFile = fileStore[call.fileId()]

        when (excelFile?.isReady) {
            null -> FileStatus.NotAvailable
            false -> FileStatus.Pending
            true -> FileStatus.Complete
        }.let {
            call.respond(it.name)
        }
    }

    get("/api/download/{fileId}") {

        val fileId = call.fileId()

        val excelFile = fileStore[fileId]

        if (excelFile == null) {
            throw FileNotFoundException(fileId)
        } else if (!excelFile.isReady) {
            throw FileNotReadyException(fileId)
        }

        call.response.header(
            HttpHeaders.ContentDisposition,
            ContentDisposition.Attachment.withParameter(
                ContentDisposition.Parameters.FileName,
                excelFile.filename
            ).toString()
        )
        call.respondOutputStream {
            excelFile.workbook!!.write(this)
        }

        fileStore.remove(fileId)
    }
}

enum class FileStatus {
    Pending, Complete, NotAvailable;
}

data class DownloadRequest(
    @JsonAlias("teksttyper") private val _teksttyper: List<Teksttype>,
    val detaljert: Boolean = false,
    val varseltype: String? = null,
    val startDato: LocalDate? = null,
    val sluttDato: LocalDate? = null,
    val inkluderStandardtekster: Boolean = false,
    @JsonAlias("minimumAntall") private val _minimumAntall: Int = 100,
    val filnavn: String? = null
) {
    val teksttyper = if (_teksttyper.isNotEmpty()) {
        _teksttyper.distinct().sorted()
    } else {
        throw IllegalArgumentException("Må spesifisere minst én teksttype")
    }

    val minimumAntall = max(100, _minimumAntall)
}

private fun RoutingCall.fileId() = request.pathVariables["fileId"]
    ?: throw IllegalArgumentException("Mangler fileId")

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

private data class ExcelFile(
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
