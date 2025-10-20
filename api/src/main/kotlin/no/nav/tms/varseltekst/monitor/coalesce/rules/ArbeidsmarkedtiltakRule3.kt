package no.nav.tms.varseltekst.monitor.coalesce.rules

object ArbeidsmarkedtiltakRule3: CoalescingRule {
    override val description = "Melding om arbedsmarkettiltak - tiltak hos arbeidssted. Utvidet igjen."

    private val ingaatt = "Din avtale om .* er inngått".toRegex()
    private val godkjenne = "Du må godkjenne en avtale om .*".toRegex()
    private val avlyst = "Avtalen om .* ble avlyst".toRegex()
    private val sluttdato = "Sluttdatoen for .*".toRegex()

    private const val ingaattReplacement = "Din avtale om <tiltak> er inngått"
    private const val godkjenneReplacement = "Du må godkjenne en avtale om <tiltak hos arbeidssted> med oppstartsdato <dato>"
    private const val avlystReplacement = "Avtalen om <tiltak hos arbeidssted> ble avlyst"
    private const val sluttdatoReplacement = "Sluttdatoen for <tiltak hos arbeidssted> er forlenget til <dato>"

    override fun ruleApplies(text: String): Boolean {
        return ingaatt.matches(text)
            || godkjenne.matches(text)
            || avlyst.matches(text)
            || sluttdato.matches(text)
    }

    override fun applyRule(text: String): String {
        return when(text) {
            in ingaatt -> ingaattReplacement
            in godkjenne -> godkjenneReplacement
            in avlyst -> avlystReplacement
            in sluttdato -> sluttdatoReplacement
            else -> text
        }
    }

    private operator fun Regex.contains(text: String) = this.matches(text)
}
