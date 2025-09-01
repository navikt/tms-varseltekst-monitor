package no.nav.tms.varseltekst.monitor.varsel

import java.time.LocalDateTime
import java.time.ZonedDateTime

fun varselJson(
    type: String,
    eventId: String,
    eksternVarsling: Boolean = true,
    spraakkode: String = "nb",
    tekst: String = "tekst",
    prefererteKanaler: List<String> = listOf("SMS", "EPOST"),
    smsVarslingstekst: String? = null,
    epostVarslingstekst: String? = null,
    epostVarslingstittel: String? = null
) = """
    {
        "@event_name": "opprettet",
        "type": "$type",
        "produsent": {
            "cluster": "cluster",
            "namespace": "namespace",
            "appnavn": "appnavn"
        },
        "varselId": "$eventId",
        "opprettet": "${ZonedDateTime.now()}",
        "ident": "12345678910",
        "innhold": { 
            "link": "http://link",
            "tekster": [
                {
                    "spraakkode": "$spraakkode",
                    "tekst": "$tekst",
                    "default": true
                },
                {
                    "spraakkode": "en",
                    "tekst": "Other text",
                    "default": false
                }
            ]
        },
        "sensitivitet": "high",
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
        "aktivFremTil": "${ZonedDateTime.now().plusDays(1)}"
    }""".trimIndent()

private fun List<String>.toJsonArray(): String {
    return if (isEmpty()) {
        "[]"
    } else {
        "[\"${joinToString("\",\"")}\"]"
    }
}
