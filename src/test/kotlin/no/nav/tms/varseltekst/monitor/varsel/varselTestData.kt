package no.nav.tms.varseltekst.monitor.varsel

import java.time.LocalDateTime

fun varselJson(
    type: String,
    eventId: String,
    eksternVarsling: Boolean = true,
    tekst: String = "tekst",
    prefererteKanaler: List<String> = listOf("SMS", "EPOST"),
    smsVarslingstekst: String? = null,
    epostVarslingstekst: String? = null,
    epostVarslingstittel: String? = null,
    source: String? = null
) = """{
        "@event_name": "aktivert",
        ${if (source != null) "\"@source\":\"$source\"," else ""}
        "varselType": "$type",
        "namespace": "namespace",
        "appnavn": "appnavn",
        "eventId": "$eventId",
        "forstBehandlet": "${LocalDateTime.now()}",
        "fodselsnummer": "12345678910",
        "tekst": "$tekst",
        "link": "http://link",
        "sikkerhetsnivaa": 4,
        "synligFremTil": "${LocalDateTime.now().plusDays(1)}",
        "aktiv": true,
        "eksternVarsling": $eksternVarsling,
        "prefererteKanaler": ${prefererteKanaler.toJsonArray()},
        "smsVarslingstekst": ${smsVarslingstekst?.let { "\"$smsVarslingstekst\"" }},
        "epostVarslingstekst": ${epostVarslingstekst?.let { "\"$epostVarslingstekst\"" }},
        "epostVarslingstittel": ${epostVarslingstittel?.let { "\"$epostVarslingstittel\"" }}
    }""".trimIndent()

private fun List<String>.toJsonArray(): String {
    return if (isEmpty()) {
        "[]"
    } else {
        "[\"${joinToString("\",\"")}\"]"
    }
}
