package no.nav.tms.varseltekst.monitor.varseltekst

import com.fasterxml.jackson.annotation.JsonAlias
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.apache.poi.ss.usermodel.Workbook
import java.time.LocalDate
import java.util.MissingResourceException
import java.util.UUID
import kotlin.math.max

fun Route.varseltekstRoutes(varseltekstRepository: VarseltekstRepository) {
    get("/api/antall/{teksttype}/totalt") {

        varseltekstRepository.tellAntallVarselteksterTotalt(
            teksttype = call.teksttype(),
            varseltype = call.varseltype(),
            startDato = call.startDato(),
            sluttDato = call.sluttDato(),
            inkluderStandardtekster = call.inkluderStandardtekster()
        ).let {
            call.respond(it)
        }
    }

    get("/api/antall/{teksttype}") {

        varseltekstRepository.tellAntallVarseltekster(
            teksttype = call.teksttype(),
            varseltype = call.varseltype(),
            startDato = call.startDato(),
            sluttDato = call.sluttDato(),
            inkluderStandardtekster = call.inkluderStandardtekster()
        ).let {
            call.respond(it)
        }
    }

    val fileStore = mutableMapOf<String, ExcelFile>()

    post("/api/download") {

        val request: DownloadRequest = call.receive()

        val workbook = if (request.detaljert) {
            varseltekstRepository.tellAntallVarseltekster(
                teksttype = request.teksttype,
                varseltype = request.varseltype,
                startDato = request.startDato,
                sluttDato = request.sluttDato,
                inkluderStandardtekster = request.inkluderStandardtekster
            ).let {
                ExcelFileWriter.antallToExcelSheet(it, request.teksttype, request.minimumAntall)
            }
        } else {
            varseltekstRepository.tellAntallVarselteksterTotalt(
                teksttype = request.teksttype,
                varseltype = request.varseltype,
                startDato = request.startDato,
                sluttDato = request.sluttDato,
                inkluderStandardtekster = request.inkluderStandardtekster
            ).let {
                ExcelFileWriter.totaltAntallToExcelSheet(it, request.teksttype, request.minimumAntall)
            }
        }

        val fileId = UUID.randomUUID().toString()
        val filename = filename(request)

        fileStore[fileId] = ExcelFile(filename, workbook)

        call.response.header(HttpHeaders.Location, "/api/download/$fileId")
        call.respond(HttpStatusCode.Accepted)
    }

    get("/api/download/{fileId}") {
        val fileId = call.fileId()

        val excelFile = fileStore.remove(fileId) ?: throw FileNotFoundException(fileId)

        call.response.header(
            HttpHeaders.ContentDisposition,
            ContentDisposition.Attachment.withParameter(
                ContentDisposition.Parameters.FileName,
                excelFile.filename
            ).toString()
        )
        call.respondOutputStream {
            excelFile.workbook.write(this)
        }
    }
}

private data class ExcelFile(
    val filename: String,
    val workbook: Workbook
)

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
    val filnavn: String? = null
) {
    val minimumAntall = max(100, _minimumAntall)
}

private fun RoutingCall.teksttype() = request.pathVariables["teksttype"]
    ?.let { Teksttype.parse(it) }
    ?: throw IllegalArgumentException("Ugyldig teksttype")


private fun RoutingCall.varseltype() = request.queryParameters["varseltype"]

private fun RoutingCall.startDato() = request.queryParameters["startDato"]
    ?.let(LocalDate::parse)

private fun RoutingCall.sluttDato() = request.queryParameters["sluttDato"]
    ?.let(LocalDate::parse)

private fun RoutingCall.inkluderStandardtekster() = request.queryParameters["standardtekster"]
    ?.toBoolean() ?: false

private fun RoutingCall.fileId() = request.pathVariables["fileId"]
    ?: throw IllegalArgumentException("Mangler fileId")

class FileNotFoundException(val fileId: String): IllegalArgumentException()
