package no.nav.tms.varseltekst.monitor.varsel

import java.time.LocalDateTime
import java.time.ZonedDateTime

fun varselJson(
    type: String,
    eventId: String,
    eksternVarsling: Boolean = true,
    tekst: String = "tekst",
    prefererteKanaler: List<String> = listOf("SMS", "EPOST"),
    smsVarslingstekst: String? = null,
    epostVarslingstekst: String? = null,
    epostVarslingstittel: String? = null,
    source: String? = "varsel-authority"
) = """{
        "@event_name": "aktivert",
        ${if (source != null) "\"@source\":\"$source\"," else ""}
        "type": "$type",
        "produsent": {
            "namespace": "namespace",
            "appnavn": "appnavn"
        },
        "varselId": "$eventId",
        "opprettet": "${ZonedDateTime.now()}",
        "ident": "12345678910",
        "innhold": { 
            "tekst": "$tekst",
            "link": "http://link"
        },
        "sensitivitet": "high",
        "aktivFremTil": "${ZonedDateTime.now().plusDays(1)}",
        ${  if(eksternVarsling) {
                """
                    "eksternVarslingBestilling": {
                        "prefererteKanaler": ${prefererteKanaler.toJsonArray()},
                        "smsVarslingstekst": ${smsVarslingstekst?.let { "\"$smsVarslingstekst\"" }},
                        "epostVarslingstekst": ${epostVarslingstekst?.let { "\"$epostVarslingstekst\"" }},
                        "epostVarslingstittel": ${epostVarslingstittel?.let { "\"$epostVarslingstittel\"" }}
                    },
                """
            } else {
                ""
            }
        }
        "aktiv": true
    }""".trimIndent()

private fun List<String>.toJsonArray(): String {
    return if (isEmpty()) {
        "[]"
    } else {
        "[\"${joinToString("\",\"")}\"]"
    }
}
