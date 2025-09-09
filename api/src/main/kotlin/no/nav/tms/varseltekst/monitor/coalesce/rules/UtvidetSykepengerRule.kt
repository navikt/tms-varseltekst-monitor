package no.nav.tms.varseltekst.monitor.coalesce.rules

object UtvidetSykepengerRule: CoalescingRule {

    override val description = "Søknad om utvidet støtte for sykepenger fra arbeidssted"

    private val pattern = "(.*)( har søkt om utvidet støtte fra NAV angående sykepenger til deg\\.)".toRegex()

    private const val replacement = "<arbeidssted>$2"

    override fun ruleApplies(text: String): Boolean {
        return pattern.matches(text)
    }

    override fun applyRule(text: String) = text.replace(pattern, replacement)
}
