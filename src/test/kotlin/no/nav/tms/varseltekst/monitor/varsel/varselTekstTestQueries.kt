package no.nav.tms.varseltekst.monitor.varsel

import no.nav.tms.varseltekst.monitor.coalesce.TekstTable
import no.nav.tms.varseltekst.monitor.database.getUtcDateTime
import no.nav.tms.varseltekst.monitor.database.list
import no.nav.tms.varseltekst.monitor.util.LocalDateTimeHelper.nowAtUtc
import java.sql.Connection
import java.sql.Types

fun Connection.insertTekst(table: TekstTable, tekst: String) {
    prepareStatement("INSERT INTO $table(tekst, first_seen_at) values(?, ?)").use {
        it.setString(1, tekst)
        it.setObject(2, nowAtUtc(), Types.TIMESTAMP)
        it.executeUpdate()
    }
}

fun Connection.selectVarsel(eventId: String): VarselOversikt {
    prepareStatement("""
        SELECT v.*, wt.tekst as webTekst, st.tekst as smsTekst, ett.tekst as epostTittel, ete.tekst as epostTekst
          FROM varsel v
            JOIN web_tekst wt on v.web_tekst = wt.id
            LEFT JOIN sms_tekst st on v.sms_tekst = st.id
            LEFT JOIN epost_tittel ett on v.epost_tittel = ett.id
            LEFT JOIN epost_tekst ete on v.epost_tekst = ete.id
        WHERE v.event_id = ?
    """.trimIndent()).use {
        it.setString(1, eventId)

        val result = it.executeQuery()

        return if (result.next()) {
            VarselOversikt(
                eventId = result.getString("event_id"),
                eventType = result.getString("event_type"),
                producerNamespace = result.getString("produsent_namespace"),
                producerAppnavn = result.getString("produsent_appnavn"),
                eksternVarsling = result.getBoolean("eksternVarsling"),
                preferertKanalSms = result.getBoolean("sms_preferert"),
                preferertKanalEpost = result.getBoolean("epost_preferert"),
                webTekst = result.getString("webTekst"),
                smsTekst = result.getString("smsTekst"),
                epostTittel = result.getString("epostTittel"),
                epostTekst = result.getString("epostTekst"),
                varseltidspunkt = result.getUtcDateTime("varseltidspunkt"),
            )
        } else {
            throw IllegalStateException()
        }
    }
}

fun Connection.antallTekster(type: TekstTable): Int = prepareStatement("select count(*) as antall from ${type.name}").use {
    it.executeQuery().run {
        next()
        getInt("antall")
    }
}

fun Connection.getTekster(type: TekstTable): List<String> = prepareStatement("select tekst from ${type.name}").use {
    it.executeQuery().list {
        getString("tekst")
    }
}
