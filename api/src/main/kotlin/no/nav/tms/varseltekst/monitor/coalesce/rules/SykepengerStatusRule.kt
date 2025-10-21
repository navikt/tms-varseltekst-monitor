package no.nav.tms.varseltekst.monitor.coalesce.rules

object SykepengerStatusRule: CoalescingRule {
    override val description = "Statusmelding i sak om sykepenger"

    private val venter = "Status i saken din om sykepenger: Vi venter.*".toRegex()
    private val mangler = "Status i saken din om sykepenger: Vi mangler fortsatt.*".toRegex()

    private const val venterReplacement = "Status i saken din om sykepenger: Vi venter på inntektsmelding fra <arbeidssted>."
    private const val manglerReplacement = "Status i saken din om sykepenger: Vi mangler fortsatt inntektsmelding fra <arbeidssted> og har sendt en påminnelse til arbeidsgiveren din om dette.Når vi får den kan vi begynne å behandle søknaden din."

    override fun ruleApplies(text: String): Boolean {
        return venter.matches(text) || mangler.matches(text)
    }

    override fun applyRule(text: String) = when {
        venter.matches(text) -> venterReplacement
        mangler.matches(text) -> manglerReplacement
        else -> text
    }
}
