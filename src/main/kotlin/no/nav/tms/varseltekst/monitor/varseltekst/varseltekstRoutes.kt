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

    post("/antall/download") {

        val request: DownloadRequest = call.receive()

        varseltekstRepository.tellAntallVarseltekster(
            teksttype = request.teksttype,
            varseltype = request.varseltype,
            startDato = request.startDato,
            sluttDato = request.sluttDato,
            inkluderStandardtekster = request.inkluderStandardtekster
        ).let {
            val workbook = ExcelWriter.antallToExcelSheet(request.teksttype, it)

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
}

data class DownloadRequest(
    @JsonAlias("teksttype") private val _teksttype: String,
    val detaljert: Boolean = false,
    val varseltype: String? = null,
    val startDato: LocalDate? = null,
    val sluttDato: LocalDate? = null,
    val inkluderStandardtekster: Boolean = false,
    @JsonAlias("minAntall") val _minAntall: Int = 100
) {
    val teksttype = Teksttype.parse(_teksttype)
    val minAntall = max(100, _minAntall)
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

