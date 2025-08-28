package no.nav.tms.varseltekst.monitor.varseltekst

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.io.FileOutputStream


fun Route.varseltekstRoutes(varseltekstRepository: VarseltekstRepository) {
    get("/antall/{teksttype}/totalt") {

        varseltekstRepository.tellAntallVarselteksterTotalt(
            teksttype = call.teksttype(),
            varseltype = call.varseltype(),
            maksAlderDager = call.maksAlderDager(),
            inkluderStandardtekster = call.inkluderStandardtekster()
        ).let {
            call.respond(it)
        }
    }

    get("/antall/{teksttype}") {

        varseltekstRepository.tellAntallVarseltekster(
            teksttype = call.teksttype(),
            varseltype = call.varseltype(),
            maksAlderDager = call.maksAlderDager(),
            inkluderStandardtekster = call.inkluderStandardtekster()
        ).let {
            call.respond(it)
        }
    }

    get("/antall/{teksttype}/totalt/download") {

        val teksttype = call.teksttype()

        varseltekstRepository.tellAntallVarselteksterTotalt(
            teksttype = teksttype,
            varseltype = call.varseltype(),
            maksAlderDager = call.maksAlderDager(),
            inkluderStandardtekster = call.inkluderStandardtekster()
        ).let {
            val workbook = ExcelWriter.totaltAntallToExcelSheet(teksttype, it)

            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(
                    ContentDisposition.Parameters.FileName,
                    "totalt_antall.xlsx"
                ).toString()
            )
            call.respondOutputStream {
                workbook.write(this)
            }
        }
    }

    get("/antall/{teksttype}/download") {

        val teksttype = call.teksttype()

        varseltekstRepository.tellAntallVarseltekster(
            teksttype = teksttype,
            varseltype = call.varseltype(),
            maksAlderDager = call.maksAlderDager(),
            inkluderStandardtekster = call.inkluderStandardtekster()
        ).let {
            val workbook = ExcelWriter.antallToExcelSheet(teksttype, it)

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
}

private fun RoutingCall.teksttype() = request.pathVariables["teksttype"]
    ?.let { Teksttype.parse(it) }
    ?: throw IllegalArgumentException("Ugyldig teksttype")


private fun RoutingCall.varseltype() = request.queryParameters["varseltype"]

private fun RoutingCall.maksAlderDager() = request.queryParameters["maksAlderDager"]
    ?.toLong()

private fun RoutingCall.inkluderStandardtekster() = request.queryParameters["standardtekster"]
    ?.toBoolean() ?: false
