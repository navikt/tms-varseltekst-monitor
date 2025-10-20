package no.nav.tms.varseltekst.monitor.coalesce.rules

object ArbeidsmarkedtiltakRule2: CoalescingRule {
    override val description = "Melding om arbedsmarkettiltak - tiltak hos arbeidssted. Utvidet."

    private val soktInn = "Du er søkt inn på arbeidsmarkedstiltaket .*".toRegex()
    private val faattPlass = "Du har fått plass på arbeidsmarkedstiltaket: .*".toRegex()
    private val mottattUtkast = "Du har mottatt et utkast til søknad på arbeidsmarkedstiltaket .*".toRegex()

    private const val soktInnReplacement = "Du er søkt inn på arbeidsmarkedstiltaket <tiltak hos arbeidssted>."
    private const val faattPlassReplacement = "Du har fått plass på arbeidsmarkedstiltaket: <tiltak hos arbeidssted>"
    private const val mottattUtkastReplacement = "Du har mottatt et utkast til søknad på arbeidsmarkedstiltaket <tiltak hos arbeidssted>. Svar på spørsmålet her."

    override fun ruleApplies(text: String): Boolean {
        return soktInn.matches(text)
            || faattPlass.matches(text)
            || mottattUtkast.matches(text)
    }

    override fun applyRule(text: String): String {
        return when(text) {
            in soktInn -> text.replace(soktInn, soktInnReplacement)
            in faattPlass -> text.replace(faattPlass, faattPlassReplacement)
            in mottattUtkast -> text.replace(mottattUtkast, mottattUtkastReplacement)
            else -> text
        }
    }

    private operator fun Regex.contains(text: String) = this.matches(text)
}
