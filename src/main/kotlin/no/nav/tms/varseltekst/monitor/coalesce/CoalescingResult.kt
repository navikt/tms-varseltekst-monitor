package no.nav.tms.varseltekst.monitor.coalesce

import no.nav.tms.varseltekst.monitor.coalesce.rules.CoalescingRule

data class CoalescingResult(
    val originalTekst: String,
    val finalTekst: String,
    val rulesApplied: List<CoalescingRule>
) {
    val isCoalesced get() = rulesApplied.isNotEmpty()

    companion object {
        fun untouched(tekst: String) = CoalescingResult(tekst, tekst, emptyList())
    }
}
