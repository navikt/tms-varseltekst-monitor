package no.nav.tms.varseltekst.monitor.varsel

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.sql.Types

private fun upsertTekstQuery(table: String) = """
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

private val upsertWebTekstIdQuery = upsertTekstQuery("web_tekst")
private val upsertSmsTekstIdQuery = upsertTekstQuery("sms_tekst")
private val upsertEpostTittelIdQuery = upsertTekstQuery("epost_tittel")
private val upsertEpostTekstIdQuery = upsertTekstQuery("epost_tekst")

private val createVarselQuery = """
    insert into varsel (
        event_id,
        eventType,
        produsent_namespace,
        produsent_appnavn,
        eksternVarsling,
        sms_preferert,
        epost_preferert,
        web_tekst,
        sms_tekst,
        epost_tittel,
        epost_tekst,
        tidspunkt
    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    on conflict do nothing
""".trimIndent()

fun Connection.upsertWebTekst(tekst: String): Int = upsertTekst(upsertWebTekstIdQuery, tekst)
fun Connection.upsertSmsTekst(tekst: String): Int = upsertTekst(upsertSmsTekstIdQuery, tekst)
fun Connection.upsertEpostTittel(tittel: String): Int = upsertTekst(upsertEpostTittelIdQuery, tittel)
fun Connection.upsertEpostTekst(tekst: String): Int = upsertTekst(upsertEpostTekstIdQuery, tekst)

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
    setObject(12, varsel.tidspunkt, Types.TIMESTAMP)
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
