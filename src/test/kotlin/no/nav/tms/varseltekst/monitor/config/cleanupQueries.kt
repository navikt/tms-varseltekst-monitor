package no.nav.tms.varseltekst.monitor.config

import kotliquery.queryOf
import no.nav.tms.varseltekst.monitor.coalesce.TekstTable
import no.nav.tms.varseltekst.monitor.database.Database
import java.sql.Connection

fun Database.deleteVarsel() = deleteFrom("varsel")

fun Database.deleteWebTekst() = deleteFrom("web_tekst")
fun Database.deleteSmsTekst() = deleteFrom("sms_tekst")
fun Database.deleteEpostTittel() = deleteFrom("epost_tittel")
fun Database.deleteEpostTekst() = deleteFrom("epost_tekst")
fun Database.deleteTekst(tekstTable: TekstTable) = deleteFrom(tekstTable.name)

fun Database.deleteCoalescingRule() = deleteFrom("coalescing_rule")
fun Database.deleteCoalescingBackLog() = deleteFrom("coalescing_backlog")

fun Database.deleteCoalescingHistoryWebTekst() = deleteFrom("coalescing_history_web_tekst")
fun Database.deleteCoalescingHistorySmsTekst() = deleteFrom("coalescing_history_sms_tekst")
fun Database.deleteCoalescingHistoryEpostTittel() = deleteFrom("coalescing_history_epost_tittel")
fun Database.deleteCoalescingHistoryEpostTekst() = deleteFrom("coalescing_history_epost_tekst")

private fun Database.deleteFrom(tableName: String) = update { queryOf("DELETE from $tableName") }

fun Database.clearAllTables() {
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
