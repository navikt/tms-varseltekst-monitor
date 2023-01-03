package no.nav.tms.varseltekst.monitor.config

import no.nav.tms.varseltekst.monitor.coalesce.TekstTable
import java.sql.Connection

fun Connection.deleteVarsel() = deleteFrom("varsel")

fun Connection.deleteWebTekst() = deleteFrom("web_tekst")
fun Connection.deleteSmsTekst() = deleteFrom("sms_tekst")
fun Connection.deleteEpostTittel() = deleteFrom("epost_tittel")
fun Connection.deleteEpostTekst() = deleteFrom("epost_tekst")
fun Connection.deleteTekst(tekstTable: TekstTable) = deleteFrom(tekstTable.name)

fun Connection.deleteCoalescingRule() = deleteFrom("coalescing_rule")
fun Connection.deleteCoalescingBackLog() = deleteFrom("coalescing_backlog")

fun Connection.deleteCoalescingHistoryWebTekst() = deleteFrom("coalescing_history_web_tekst")
fun Connection.deleteCoalescingHistorySmsTekst() = deleteFrom("coalescing_history_sms_tekst")
fun Connection.deleteCoalescingHistoryEpostTittel() = deleteFrom("coalescing_history_epost_tittel")
fun Connection.deleteCoalescingHistoryEpostTekst() = deleteFrom("coalescing_history_epost_tekst")

private fun Connection.deleteFrom(tableName: String) = prepareStatement("DELETE from $tableName").executeUpdate()

fun Connection.clearAllTables() {
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
