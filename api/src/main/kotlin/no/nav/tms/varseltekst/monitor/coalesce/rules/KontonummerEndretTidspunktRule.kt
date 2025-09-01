package no.nav.tms.varseltekst.monitor.coalesce.rules

object KontonummerEndretTidspunktRule: CoalescingRule {
    override val description = "Tidspunkt i varsel om endret kontonummer"

    private val dateTimePattern = "[0-9]{1,2}\\.[0-9]{1,2}\\. kl\\. [0-9]{2}:[0-9]{2}".toRegex()
    private const val replacement = "<tidspunkt>"

    override fun ruleApplies(text: String): Boolean {
        return dateTimePattern.containsMatchIn(text)
    }

    override fun applyRule(text: String): String {
        return text.replace(dateTimePattern, replacement)
    }
}
