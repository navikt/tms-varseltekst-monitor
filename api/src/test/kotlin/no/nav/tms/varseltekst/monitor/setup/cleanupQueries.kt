package no.nav.tms.varseltekst.monitor.setup

import kotliquery.queryOf
import no.nav.tms.common.postgres.PostgresDatabase
import no.nav.tms.varseltekst.monitor.coalesce.TekstTable

fun PostgresDatabase.deleteVarsel() = deleteFrom("varsel")

fun PostgresDatabase.deleteWebTekst() = deleteFrom("web_tekst")
fun PostgresDatabase.deleteSmsTekst() = deleteFrom("sms_tekst")
fun PostgresDatabase.deleteEpostTittel() = deleteFrom("epost_tittel")
fun PostgresDatabase.deleteEpostTekst() = deleteFrom("epost_tekst")
fun PostgresDatabase.deleteTekst(tekstTable: TekstTable) = deleteFrom(tekstTable.name)

fun PostgresDatabase.deleteCoalescingRule() = deleteFrom("coalescing_rule")
fun PostgresDatabase.deleteCoalescingBackLog() = deleteFrom("coalescing_backlog")

fun PostgresDatabase.deleteCoalescingHistoryWebTekst() = deleteFrom("coalescing_history_web_tekst")
fun PostgresDatabase.deleteCoalescingHistorySmsTekst() = deleteFrom("coalescing_history_sms_tekst")
fun PostgresDatabase.deleteCoalescingHistoryEpostTittel() = deleteFrom("coalescing_history_epost_tittel")
fun PostgresDatabase.deleteCoalescingHistoryEpostTekst() = deleteFrom("coalescing_history_epost_tekst")

private fun PostgresDatabase.deleteFrom(tableName: String) = update { queryOf("DELETE from $tableName") }

fun PostgresDatabase.clearAllTables() {
    deleteCoalescingHistoryWebTekst()
    deleteCoalescingHistorySmsTekst()
    deleteCoalescingHistoryEpostTittel()
    deleteCoalescingHistoryEpostTekst()
    deleteCoalescingBackLog()
    deleteVarsel()
    deleteWebTekst()
    deleteSmsTekst()
    deleteEpostTittel()
    deleteEpostTekst()
    deleteCoalescingRule()
}
