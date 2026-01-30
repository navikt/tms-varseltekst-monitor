package no.nav.tms.varseltekst.monitor.varsel

import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tms.common.postgres.PostgresDatabase
import no.nav.tms.varseltekst.monitor.coalesce.TekstTable
import no.nav.tms.varseltekst.monitor.util.LocalDateTimeHelper.nowAtUtc
import no.nav.tms.varseltekst.monitor.util.singleInTx
import no.nav.tms.varseltekst.monitor.util.singleOrNullInTx
import no.nav.tms.varseltekst.monitor.util.transaction

class VarselRepository(private val database: PostgresDatabase) {

    fun persistVarsel(varsel: VarselOversikt) {
        VarselDto(
            eventId = varsel.eventId,
            eventType = varsel.eventType,
            producerNamespace = varsel.producerNamespace,
            producerAppnavn = varsel.producerAppnavn,
            eksternVarsling = varsel.eksternVarsling,
            preferertKanalSms = varsel.preferertKanalSms,
            preferertKanalEpost = varsel.preferertKanalEpost,
            webTekstRef = persistWebTekst(varsel.webTekst),
            smsTekstRef = varsel.smsTekst?.let { persistSmsTekst(it) },
            epostTittelRef = varsel.epostTittel?.let { persistEpostTittel(it) },
            epostTekstRef = varsel.epostTekst?.let { persistEpostTekst(it) },
            varseltidspunkt = varsel.varseltidspunkt,
            tidspunkt = nowAtUtc()
        ).let { insertVarsel(it) }
    }

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
        ) values (
            :eventId,
            :eventType,
            :produsentNamespace,
            :produsentAppnavn,
            :eksternVarsling,
            :smsPreferert,
            :epostPreferert,
            :webTekst,
            :smsTekst,
            :epostTittel,
            :epostTekst,
            :varseltidspunkt,
            :tidspunkt
        )
        on conflict do nothing
    """

    private fun insertVarsel(varselDto: VarselDto) = database.update {
        queryOf(
            createVarselQuery,
            mapOf(
                "eventId" to varselDto.eventId,
                "eventType" to varselDto.eventType,
                "produsentNamespace" to varselDto.producerNamespace,
                "produsentAppnavn" to varselDto.producerAppnavn,
                "eksternVarsling" to varselDto.eksternVarsling,
                "smsPreferert" to varselDto.preferertKanalSms,
                "epostPreferert" to varselDto.preferertKanalEpost,
                "webTekst" to varselDto.webTekstRef,
                "smsTekst" to varselDto.smsTekstRef,
                "epostTittel" to varselDto.epostTittelRef,
                "epostTekst" to varselDto.epostTekstRef,
                "varseltidspunkt" to varselDto.varseltidspunkt,
                "tidspunkt" to varselDto.tidspunkt,
            )
        )
    }

    private fun selectTekstQuery(table: TekstTable) = """
       SELECT id FROM $table WHERE tekst = :tekst
    """

    private fun insertTekstQuery(table: TekstTable) = """
        INSERT INTO $table(tekst, first_seen_at)
          VALUES(:tekst, :firstSeenAt)
        RETURNING ID
    """.trimIndent()

    private val selectWebTekstQuery = selectTekstQuery(TekstTable.WEB_TEKST)
    private val selectSmsTekstQuery = selectTekstQuery(TekstTable.SMS_TEKST)
    private val selectEpostTittelQuery = selectTekstQuery(TekstTable.EPOST_TITTEL)
    private val selectEpostTekstQuery = selectTekstQuery(TekstTable.EPOST_TEKST)

    private val insertWebTekstQuery = insertTekstQuery(TekstTable.WEB_TEKST)
    private val insertSmsTekstQuery = insertTekstQuery(TekstTable.SMS_TEKST)
    private val insertEpostTittelQuery = insertTekstQuery(TekstTable.EPOST_TITTEL)
    private val insertEpostTekstQuery = insertTekstQuery(TekstTable.EPOST_TEKST)

    fun upsertWebTekst(tekst: String): Int = upsertTekst(selectWebTekstQuery, insertWebTekstQuery, tekst)
    fun upsertSmsTekst(tekst: String): Int = upsertTekst(selectSmsTekstQuery, insertSmsTekstQuery, tekst)
    fun upsertEpostTittel(tittel: String): Int = upsertTekst(selectEpostTittelQuery, insertEpostTittelQuery, tittel)
    fun upsertEpostTekst(tekst: String): Int = upsertTekst(selectEpostTekstQuery, insertEpostTekstQuery, tekst)

    private fun upsertTekst(selectQuery: String, insertQuery: String, tekst: String) = database.transaction {
        val existingId = existingId(selectQuery, tekst)

        if (existingId == null) {
            insertTekst(insertQuery, tekst)
        } else {
            existingId
        }
    }

    private fun TransactionalSession.existingId(selectQuery: String, tekst: String) = singleOrNullInTx {
        queryOf(
            selectQuery,
            mapOf("tekst" to tekst)
        )
            .map { it.int("id") }
            .asSingle
    }

    private fun TransactionalSession.insertTekst(insertQuery: String, tekst: String) = singleInTx {
        queryOf(
            insertQuery,
            mapOf(
                "tekst" to tekst,
                "firstSeenAt" to nowAtUtc()
            )
        )
            .map { it.int("id") }
            .asSingle
    }

    private fun persistWebTekst(tekst: String): Int = upsertWebTekst(tekst)
    private fun persistSmsTekst(tekst: String): Int = upsertSmsTekst(tekst)
    private fun persistEpostTittel(tekst: String): Int = upsertEpostTittel(tekst)
    private fun persistEpostTekst(tekst: String): Int = upsertEpostTekst(tekst)
}
