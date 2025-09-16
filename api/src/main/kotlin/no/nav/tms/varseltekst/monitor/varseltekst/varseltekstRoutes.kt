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

fun Route.varseltekstRoutes(queryHandler: VarselDownloadQueryHandler) {

    val log = KotlinLogging.logger { }

    val fileStore = mutableMapOf<String, ExcelFile>()

    val queryScope = CoroutineScope(Dispatchers.Default + Job())

    post("/api/download") {

        val request: DownloadRequest = call.receive()

        log.info { "Starting query" }

        val queryJob = queryHandler.startQuery(request)

        val fileId = UUID.randomUUID().toString()
        val filename = filename(request)

        log.info { "Pointing client to future file location" }

        fileStore[fileId] = ExcelFile.waiting(filename)

        call.response.header(HttpHeaders.Location, "/api/download/$fileId")
        call.respond(HttpStatusCode.Accepted)

        queryScope.launch {
            fileStore[fileId]!!.workbook = queryJob.await()
        }
    }

    get("/api/download/{fileId}/status") {
        val excelFile = fileStore[call.fileId()]

        when (excelFile?.isReady) {
            null -> call.respond(StatusResponse.NotAvailable.name)
            false -> call.respond(StatusResponse.Pending.name)
            true -> call.respond(StatusResponse.Complete.name)
        }
    }

    get("/api/download/{fileId}") {
        val fileId = call.fileId()

        val excelFile = fileStore[fileId] ?: throw FileNotFoundException(fileId)

        if (!excelFile.isReady) {
            call.respond(HttpStatusCode.Processing)
        } else {
            fileStore.remove(fileId)

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
        }
    }
}

enum class StatusResponse{
    Pending, Complete, NotAvailable;
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
data class DownloadRequest(
    @JsonAlias("teksttype") private val _teksttype: Teksttype? = null,
    @JsonAlias("teksttyper") private val _teksttyper: List<Teksttype> = emptyList(),
    val detaljert: Boolean = false,
    val varseltype: String? = null,
    val startDato: LocalDate? = null,
    val sluttDato: LocalDate? = null,
    val inkluderStandardtekster: Boolean = false,
    @JsonAlias("minimumAntall") private val _minimumAntall: Int = 100,
    val filnavn: String? = null
) {
    val teksttyper get() = if (_teksttyper.isNotEmpty()) {
        _teksttyper
    } else if (_teksttype != null) {
        listOf(_teksttype)
    } else {
        throw IllegalArgumentException("Må spesifisere minst én teksttype")
    }

    val minimumAntall = max(100, _minimumAntall)
}

private fun RoutingCall.fileId() = request.pathVariables["fileId"]
    ?: throw IllegalArgumentException("Mangler fileId")

class FileNotFoundException(val fileId: String): IllegalArgumentException()
