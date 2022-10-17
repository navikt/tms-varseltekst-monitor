package no.nav.tms.varseltekst.monitor.varsel

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Types

private val createQuery = """
    insert into varseltekster_v1 (
        produsent_namespace,
        produsent_appnavn,
        sms_preferert,
        epost_preferert,
        varsel_tekst,
        sms_tekst,
        epost_tittel,
        epost_tekst,
        tidspunkt
    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
""".trimIndent()


fun Connection.createVarseltekster(varseltekster: Varseltekster) =
    prepareStatement(createQuery).use {
        it.setParameters(varseltekster)
        it.executeUpdate()
    }

private fun PreparedStatement.setParameters(varseltekster: Varseltekster) {
    setString(1, varseltekster.producer.namespace)
    setString(2, varseltekster.producer.appnavn)
    setBoolean(3, varseltekster.preferertKanalSms)
    setBoolean(4, varseltekster.preferertKanalEpost)
    setString(5, varseltekster.tekst)
    setString(6, varseltekster.smsTekst)
    setString(7, varseltekster.epostTittel)
    setString(8, varseltekster.epostTekst)
    setObject(9, varseltekster.tidspunkt, Types.TIMESTAMP)
}
