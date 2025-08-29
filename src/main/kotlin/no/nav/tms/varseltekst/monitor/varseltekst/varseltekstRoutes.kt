package no.nav.tms.varseltekst.monitor.varseltekst

import com.fasterxml.jackson.annotation.JsonAlias
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDate
import kotlin.math.max

fun Route.varseltekstRoutes(varseltekstRepository: VarseltekstRepository) {
    get("/antall/{teksttype}/totalt") {

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

    get("/antall/{teksttype}") {

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

    suspend fun RoutingContext.downloadAntall(request: DownloadRequest) {
        varseltekstRepository.tellAntallVarseltekster(
            teksttype = request.teksttype,
            varseltype = request.varseltype,
            startDato = request.startDato,
            sluttDato = request.sluttDato,
            inkluderStandardtekster = request.inkluderStandardtekster
        ).let {
            val workbook = ExcelWriter.antallToExcelSheet(request.teksttype, it, request.minimumAntall)

            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(
                    ContentDisposition.Parameters.FileName,
                    "antall.xlsx"
                ).toString()
            )
            call.respondOutputStream {
                workbook.write(this)
            }
        }
    }

    suspend fun RoutingContext.downloadTotaltAntall(request: DownloadRequest) {
        varseltekstRepository.tellAntallVarselteksterTotalt(
            teksttype = request.teksttype,
            varseltype = request.varseltype,
            startDato = request.startDato,
            sluttDato = request.sluttDato,
            inkluderStandardtekster = request.inkluderStandardtekster
        ).let {
            val workbook = ExcelWriter.totaltAntallToExcelSheet(request.teksttype, it, request.minimumAntall)

            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(
                    ContentDisposition.Parameters.FileName,
                    filnavn(request)
                ).toString()
            )
            call.respondOutputStream {
                workbook.write(this)
            }
        }
    }

    post("/antall/download") {

        val request: DownloadRequest = call.receive()

        if (request.detaljert) {
            downloadAntall(request)
        } else {
            downloadTotaltAntall(request)
        }
    }
}

private fun filnavn(request: DownloadRequest): String {
    return when {
        request.filnavn == null -> "${LocalDate.now()}-varseltekster-${request.teksttype.name.lowercase()}-${if (request.detaljert) "" else "totalt-"}antall.xlsx"
        request.filnavn.endsWith(".xlsx") -> request.filnavn
        else -> "${request.filnavn}.xlsx"
    }
}
data class DownloadRequest(
    @JsonAlias("teksttype") private val _teksttype: String,
    val detaljert: Boolean = false,
    val varseltype: String? = null,
    val startDato: LocalDate? = null,
    val sluttDato: LocalDate? = null,
    val inkluderStandardtekster: Boolean = false,
    @JsonAlias("minimumAntall") private val _minimumAntall: Int = 100,
    val filnavn: String? = null
) {
    val teksttype = Teksttype.parse(_teksttype)
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

