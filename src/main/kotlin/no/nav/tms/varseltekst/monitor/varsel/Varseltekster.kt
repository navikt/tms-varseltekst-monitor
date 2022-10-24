package no.nav.tms.varseltekst.monitor.varsel

import java.time.LocalDateTime

data class Varseltekster(
    val producer: Producer,
    val preferertKanalSms: Boolean,
    val preferertKanalEpost: Boolean,
    val tekst: String,
    val smsTekst: String?,
    val epostTittel: String?,
    val epostTekst: String?,
    val tidspunkt: LocalDateTime
)

data class Producer(
    val namespace: String,
    val appnavn: String
)

data class Varsel_2(
    val eventId: String,
    val eventType: String,
    val producerNamespace: String,
    val producerAppnavn: String,
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
    val preferertKanalSms: Boolean,
    val preferertKanalEpost: Boolean,
    val webTekstRef: Int,
    val smsTekstRef: Int?,
    val epostTittelRef: Int?,
    val epostTekstRef: Int?,
    val varseltidspunkt: LocalDateTime,
    val tidspunkt: LocalDateTime
)
