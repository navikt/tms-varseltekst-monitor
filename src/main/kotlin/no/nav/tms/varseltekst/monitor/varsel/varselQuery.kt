package no.nav.tms.varseltekst.monitor.varsel

import no.nav.tms.varseltekst.monitor.coalesce.TekstTable
import no.nav.tms.varseltekst.monitor.coalesce.TekstTable.*
import no.nav.tms.varseltekst.monitor.coalesce.VarselTekst
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

private fun upsertTekstQuery(table: TekstTable) = """
    WITH input_tekst(tekst) AS (
        VALUES(?)
    ),
         inserted_tekst AS (
             INSERT INTO $table(tekst) SELECT tekst FROM input_tekst
                 ON CONFLICT (tekst) DO NOTHING
             RETURNING id
         )
    SELECT id FROM inserted_tekst
        UNION ALL
    SELECT t.id FROM input_tekst 
        JOIN $table t USING (tekst);
""".trimIndent()

private val upsertWebTekstQuery = upsertTekstQuery(WEB_TEKST)
private val upsertSmsTekstQuery = upsertTekstQuery(SMS_TEKST)
private val upsertEpostTittelQuery = upsertTekstQuery(EPOST_TITTEL)
private val upsertEpostTekstQuery = upsertTekstQuery(EPOST_TEKST)

private val createVarselQuery = """
    insert into varsel (
        event_id,
        event_type,
        produsent_namespace,
        produsent_appnavn,
        eksternVarsling,
        sms_preferert,
        epost_preferert,
        web_tekst,
        sms_tekst,
        epost_tittel,
        epost_tekst,
        varseltidspunkt,
        tidspunkt
    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    on conflict do nothing
""".trimIndent()

fun Connection.upsertWebTekst(tekst: String): Int = upsertTekst(upsertWebTekstQuery, tekst)
fun Connection.upsertSmsTekst(tekst: String): Int = upsertTekst(upsertSmsTekstQuery, tekst)
fun Connection.upsertEpostTittel(tittel: String): Int = upsertTekst(upsertEpostTittelQuery, tittel)
fun Connection.upsertEpostTekst(tekst: String): Int = upsertTekst(upsertEpostTekstQuery, tekst)

fun Connection.insertVarsel(varseltekster: VarselDto) =
    prepareStatement(createVarselQuery).use {
        it.setParameters(varseltekster)
        it.executeUpdate()
    }

private fun PreparedStatement.setParameters(varsel: VarselDto) {
    setString(1, varsel.eventId)
    setString(2, varsel.eventType)
    setString(3, varsel.producerNamespace)
    setString(4, varsel.producerAppnavn)
    setBoolean(5, varsel.eksternVarsling)
    setBoolean(6, varsel.preferertKanalSms)
    setBoolean(7, varsel.preferertKanalEpost)
    setInt(8, varsel.webTekstRef)
    setObject(9, varsel.smsTekstRef, Types.INTEGER)
    setObject(10, varsel.epostTittelRef, Types.INTEGER)
    setObject(11, varsel.epostTekstRef, Types.INTEGER)
    setObject(12, varsel.varseltidspunkt, Types.TIMESTAMP)
    setObject(13, varsel.tidspunkt, Types.TIMESTAMP)
}

private fun Connection.upsertTekst(selectQuery: String, tekst: String): Int {
    return prepareStatement(selectQuery).use {
        it.setString(1, tekst)

        it.executeQuery().let { result ->
            if (result.next()) {
                result.getInt("id")
            } else {
                throw IllegalStateException("Fant ikke tekst i db.")
            }
        }
    }
}
