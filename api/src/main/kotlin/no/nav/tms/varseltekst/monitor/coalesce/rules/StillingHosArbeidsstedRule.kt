package no.nav.tms.varseltekst.monitor.coalesce.rules

object StillingHosArbeidsstedRule: CoalescingRule {
    override val description = "Stilling hos arbeidssted av formen '«stilling» hos «arbeidssted»'"

    private val stillingPattern = "«.*» hos «.*»".toRegex()

    private const val replacement = "<stilling> hos <arbeidssted>"

    override fun ruleApplies(text: String): Boolean {
        return stillingPattern.containsMatchIn(text)
    }

    override fun applyRule(text: String): String {
        return text.replace(stillingPattern, replacement)
    }
}
