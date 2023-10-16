package no.nav.tms.varseltekst.monitor.coalesce.rules

import no.nav.tms.varseltekst.monitor.locale.DayMonthHelper
import java.util.*

object DayOfWeekDateTimeRule: CoalescingRule {

    override val description = "Ukedag med dato og tidspunkt"

    private val norskBokmaalRegex = buildDateRegex()
    private const val replacement = "<tidspunkt>"

    override fun ruleApplies(text: String): Boolean {
        return norskBokmaalRegex.containsMatchIn(text)
    }

    override fun applyRule(text: String) = text.replace(norskBokmaalRegex, replacement)

    private fun buildDateRegex(): Regex {
        val localeNB = Locale("nb", "NO")

        val dayRegexPart = DayMonthHelper.daysOfWeek(localeNB).joinToString("|")
        val monthRegexPart = DayMonthHelper.monthsOfYear(localeNB).joinToString("|")

        return "($dayRegexPart) [0-9]{0,2}\\. ($monthRegexPart) kl\\. [0-9]{2}:[0-9]{2}".toRegex()
    }
}
