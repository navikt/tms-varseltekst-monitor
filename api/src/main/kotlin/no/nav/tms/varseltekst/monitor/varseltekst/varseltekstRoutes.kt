package no.nav.tms.varseltekst.monitor.varseltekst

import com.fasterxml.jackson.annotation.JsonAlias
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import java.time.LocalDate
import java.util.*
import kotlin.math.max

fun Route.varseltekstRoutes(queryService: VarseltekstQueryService) {

    val log = KotlinLogging.logger { }

    post("/api/download") {

        val request: DownloadRequest = call.receive()

        log.info { "Starting query" }

        val fileId = queryService.processRequestAsync(request)

        log.info { "Pointing client to future file location" }

        call.response.header(HttpHeaders.Location, "/api/download/$fileId")
        call.respond(HttpStatusCode.Accepted)
    }

    get("/api/download/{fileId}/status") {
        call.respond(queryService.fileStatus(call.fileId()))
    }

    get("/api/download/{fileId}") {

        val excelFile = queryService.releaseFile(call.fileId())

        if (!excelFile.isReady) {
            call.respond(HttpStatusCode.Processing)
        } else {
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
