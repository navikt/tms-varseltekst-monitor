package no.nav.tms.varseltekst.monitor.coalesce.rules

object DokumentTittelRule: CoalescingRule {
    override val description = "Tittel på brev"

    private val pattern = "(Du har fått .* som du må lese: )(.*)(\\. Les .*)".toRegex()

    private val replacement = "$1<tittel>$3"

    override fun ruleApplies(text: String): Boolean {
        return pattern.matches(text)
    }

    override fun applyRule(text: String) = text.replace(pattern, replacement)
}
