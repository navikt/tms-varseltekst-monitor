package no.nav.tms.varseltekst.monitor.coalesce.rules


object InntektsmeldingRule: CoalescingRule {
    override val description = "Tittel og dato for inntektsmelding"

    private val pattern = "(Vi mangler inntektsmeldingen fra )(.*)( for sykefraværet som startet )(.*)(\\.)".toRegex()

    private const val replacement = "$1<arbeidssted>$3<dato>$5"

    override fun ruleApplies(text: String): Boolean {
        return pattern.matches(text)
    }

    override fun applyRule(text: String) = text.replace(pattern, replacement)

}
