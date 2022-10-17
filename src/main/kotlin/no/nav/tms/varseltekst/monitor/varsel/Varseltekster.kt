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
