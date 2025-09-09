package no.nav.tms.varseltekst.monitor.coalesce.rules

object SykefravaerOrgRule: CoalescingRule {
    override val description = "Arbeidssted som har søkt om dekning av sykepenger"

    private val pattern = "(.*)( har søkt om at N[aA][vV] dekker sykepenger fra første dag av sykefraværet ditt\\.)".toRegex()

    private const val replacement = "<arbeidssted>$2"

    override fun ruleApplies(text: String): Boolean {
        return pattern.matches(text)
    }

    override fun applyRule(text: String) = text.replace(pattern, replacement)
}
