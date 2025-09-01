package no.nav.tms.varseltekst.monitor.varsel

import kotliquery.Row
import kotliquery.queryOf
import no.nav.tms.varseltekst.monitor.coalesce.TekstTable
import no.nav.tms.varseltekst.monitor.setup.Database
import no.nav.tms.varseltekst.monitor.util.LocalDateTimeHelper.nowAtUtc

fun Database.insertTekst(table: TekstTable, tekst: String) = update {
    queryOf("INSERT INTO $table(tekst, first_seen_at) values(:tekst, :firstSeenAt)",
        mapOf(
            "tekst" to tekst,
            "firstSeenAt" to nowAtUtc()
        )
    )
}

fun Database.selectVarsel(eventId: String) = single {
    queryOf("""
        SELECT v.*, wt.tekst as webTekst, st.tekst as smsTekst, ett.tekst as epostTittel, ete.tekst as epostTekst
          FROM varsel v
            JOIN web_tekst wt on v.web_tekst = wt.id
            LEFT JOIN sms_tekst st on v.sms_tekst = st.id
            LEFT JOIN epost_tittel ett on v.epost_tittel = ett.id
            LEFT JOIN epost_tekst ete on v.epost_tekst = ete.id
        WHERE v.event_id = :eventId
    """,
        mapOf("eventId" to eventId)
    )
        .map(toVarseloversikt())
        .asSingle
}

private fun toVarseloversikt(): (Row) -> VarselOversikt = { result ->
    VarselOversikt(
        eventId = result.string("event_id"),
        eventType = result.string("event_type"),
        producerNamespace = result.string("produsent_namespace"),
        producerAppnavn = result.string("produsent_appnavn"),
        eksternVarsling = result.boolean("eksternVarsling"),
        preferertKanalSms = result.boolean("sms_preferert"),
        preferertKanalEpost = result.boolean("epost_preferert"),
        webTekst = result.string("webTekst"),
        smsTekst = result.stringOrNull("smsTekst"),
        epostTittel = result.stringOrNull("epostTittel"),
        epostTekst = result.stringOrNull("epostTekst"),
        varseltidspunkt = result.localDateTime("varseltidspunkt")
    )
}

fun Database.antallTekster(type: TekstTable): Int = single {
    queryOf("select count(*) as antall from ${type.name}")
        .map { it.int("antall") }
        .asSingle
}

fun Database.getTekster(type: TekstTable): List<String> = list {
    queryOf("select tekst from ${type.name}")
        .map { it.string("tekst") }
        .asList
}
