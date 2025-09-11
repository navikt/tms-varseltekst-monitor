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
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

fun Route.varseltekstRoutes(queryHandler: VarselDownloadQueryHandler) {

    val log = KotlinLogging.logger { }

    val fileStore = mutableMapOf<String, ExcelFile>()

    val waitingScope = CoroutineScope(Dispatchers.Default + Job())

    post("/api/download") {

        val request: DownloadRequest = call.receive()

        log.info { "Starting query" }

        val queryJob = queryHandler.startQuery(request)

        val fileId = UUID.randomUUID().toString()
        val filename = filename(request)

        if (request.deferDownloadAfterMs == null) {

            log.info { "Waiting for query indefinitely" }

            fileStore[fileId] = ExcelFile.ready(filename, queryJob.await())

            log.info { "Query done" }

            call.response.header(HttpHeaders.Location, "/api/download/$fileId")
            call.respond(HttpStatusCode.Accepted)
        } else {


            val deferAfter = request.deferDownloadAfterMs.milliseconds
            val start = TimeSource.Monotonic.markNow()

            log.info { "Waiting for query up to $deferAfter" }

            while (!queryJob.isCompleted && start.elapsedNow() < deferAfter) {
                delay(100)
            }

            log.info { "Stopped waiting after ${start.elapsedNow()}" }

            if (queryJob.isCompleted) {

                log.info { "Query complete, serving file" }

                fileStore[fileId] = ExcelFile.ready(filename, queryJob.await())

                call.response.header(HttpHeaders.Location, "/api/download/$fileId")
                call.respond(HttpStatusCode.Accepted)
            } else {

                log.info { "Query incomplete, redirecting to waiting room" }

                fileStore[fileId] = ExcelFile.waiting(filename)

                call.response.header(HttpHeaders.Location, "/venterom/$fileId")
                call.respond(HttpStatusCode.Found)

                waitingScope.launch {
                    fileStore[fileId]!!.workbook = queryJob.await()
                }
            }
        }
    }

    head("/api/download/{fileId}") {
        val excelFile = fileStore[call.fileId()]

        when (excelFile?.isReady) {
            null -> call.respond(HttpStatusCode.NotFound)
            false -> call.respond(HttpStatusCode.Processing)
            true -> call.respond(HttpStatusCode.OK)
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

private data class ExcelFile(
    val filename: String,
    var workbook: Workbook?,
) {
    val isReady: Boolean get() = workbook != null

    companion object {
        fun ready(filename: String, workbook: Workbook) = ExcelFile(
            filename = filename,
            workbook = workbook,
        )

        fun waiting(filename: String) = ExcelFile(
            filename = filename,
            workbook = null,
        )
    }
}

private fun filename(request: DownloadRequest): String {
    return when {
        request.filnavn == null -> "${LocalDate.now()}-varseltekster-${request.teksttype.name.lowercase()}-${if (request.detaljert) "" else "totalt-"}antall.xlsx"
        request.filnavn.endsWith(".xlsx") -> request.filnavn
        else -> "${request.filnavn}.xlsx"
    }
}
data class DownloadRequest(
    val teksttype: Teksttype,
    val detaljert: Boolean = false,
    val varseltype: String? = null,
    val startDato: LocalDate? = null,
    val sluttDato: LocalDate? = null,
    val inkluderStandardtekster: Boolean = false,
    @JsonAlias("minimumAntall") private val _minimumAntall: Int = 100,
    val filnavn: String? = null,
    val deferDownloadAfterMs: Long? = 5000
) {
    val minimumAntall = max(100, _minimumAntall)
}

private fun RoutingCall.fileId() = request.pathVariables["fileId"]
    ?: throw IllegalArgumentException("Mangler fileId")

class FileNotFoundException(val fileId: String): IllegalArgumentException()
