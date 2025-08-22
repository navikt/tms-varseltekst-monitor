package no.nav.tms.varseltekst.monitor.varseltekst

import io.ktor.server.routing.*
import io.ktor.server.response.respond

fun Route.varseltekstRoutes(varseltekstRepository: VarseltekstRepository) {
    get("/antall/{teksttype}/totalt") {

        varseltekstRepository.finnForenkletSammendrag(
            teksttype = call.teksttype(),
            varseltype = call.varseltype(),
            maksAlderDager = call.maksAlderDager()
        ).let {
            call.respond(it)
        }
    }
}

private fun RoutingCall.teksttype() = request.pathVariables["teksttype"]
    ?.let { Teksttype.parse(it) }
    ?: throw IllegalArgumentException("Ugyldig teksttype")


private fun RoutingCall.varseltype() = request.pathVariables["varseltype"]


private fun RoutingCall.maksAlderDager() = request.pathVariables["maksAlderDager"]
    ?.toLong()
