package no.nav.tms.varseltekst.monitor.coalesce.rules

object ArbeidsmarkedtiltakRule: CoalescingRule {
    override val description = "Melding om arbedsmarkettiltak - tiltak hos arbeidssted"

    private val utkast = "(Du har mottatt et utkast til påmelding på arbeidsmarkedstiltaket: )(.*)(\\. Svar på spørsmålet her\\.)".toRegex()
    private val paameldt = "(Du er meldt på arbeidsmarkedstiltaket: )(.*)".toRegex()
    private val endring = "(Ny endring på arbeidsmarkedstiltaket: )(.*)".toRegex()

    private const val replacement = "$1<tiltak hos arbeidssted>."
    private const val replacementUtkast = "$1<tiltak hos arbeidssted>$3"

    override fun ruleApplies(text: String): Boolean {
        return utkast.matches(text)
            || paameldt.matches(text)
            || endring.matches(text)
    }

    override fun applyRule(text: String): String {
        return when(text) {
            in utkast -> text.replace(utkast, replacementUtkast)
            in paameldt -> text.replace(paameldt, replacement)
            in endring -> text.replace(endring, replacement)
            else -> text
        }
    }

    private operator fun Regex.contains(text: String) = this.matches(text)
}
