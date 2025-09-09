package no.nav.tms.varseltekst.monitor.varsel

import java.time.LocalDateTime
import java.time.ZonedDateTime

data class AktivertVarsel(
    val type: String,
    val varselId: String,
    val innhold: Innhold,
    val produsent: Produsent,
    val eksternVarslingBestilling: EksternVarslingBestilling? = null,
    val opprettet: ZonedDateTime
)

data class Innhold(
    val link: String?,
    val tekster: List<WebTekst>
) {
    fun defaultTekst() = when {
        tekster.size == 1 -> tekster.first().tekst
        tekster.size > 1 -> tekster.first { it.default }.tekst
        else -> throw IllegalArgumentException("Må definere minst 1 tekst")
    }
}

data class WebTekst(
    val tekst: String,
    val spraakkode: String,
    val default: Boolean
)

data class Produsent(
    val namespace: String,
    val appnavn: String
)

data class EksternVarslingBestilling(
    val prefererteKanaler: List<String>,
    val smsVarslingstekst: String?,
    val epostVarslingstekst: String?,
    val epostVarslingstittel: String?,
)

data class VarselOversikt(
    val eventId: String,
    val eventType: String,
    val producerNamespace: String,
    val producerAppnavn: String,
    val eksternVarsling: Boolean,
    val preferertKanalSms: Boolean,
    val preferertKanalEpost: Boolean,
    val webTekst: String,
    val smsTekst: String?,
    val epostTittel: String?,
    val epostTekst: String?,
    val varseltidspunkt: LocalDateTime
)

data class VarselDto(
    val eventId: String,
    val eventType: String,
    val producerNamespace: String,
    val producerAppnavn: String,
    val eksternVarsling: Boolean,
    val preferertKanalSms: Boolean,
    val preferertKanalEpost: Boolean,
    val webTekstRef: Int,
    val smsTekstRef: Int?,
    val epostTittelRef: Int?,
    val epostTekstRef: Int?,
    val varseltidspunkt: LocalDateTime,
    val tidspunkt: LocalDateTime
)
