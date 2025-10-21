package no.nav.tms.varseltekst.monitor.coalesce.rules

object FullmaktNavnRule2: CoalescingRule {
    override val description = "Navn i varsler om fullmakt. V2"

    private val opprettetFullmakt = "Det ble opprettet en fullmakt og du kan nå representere .*".toRegex()
    private val avsluttetFullmakt = "Du har avsluttet fullmakten du tidligere har gitt til .*".toRegex()
    private val endretFullmakt = "Du har endret fullmakten du tidligere har gitt til .*".toRegex()
    private val gittFullmakt = "Du har gitt fullmakt til .*".toRegex()

    private const val opprettetFullmaktReplacement = "Det ble opprettet en fullmakt og du kan nå representere <representert>. Se Fullmakter på nav.no for mer informasjon."
    private const val avsluttetFullmaktReplacement = "Du har avsluttet fullmakten du tidligere har gitt til <fullmektig>. Se Fullmakter på nav.no for mer informasjon. Har du ikke avsluttet en fullmakt ta kontakt med Nav på tlf 55 55 33 33."
    private const val endretFullmaktReplacement = "Du har endret fullmakten du tidligere har gitt til <fullmektig>. Se Fullmakter på nav.no for mer informasjon. Har du ikke endret en fullmakt ta kontakt med Nav på tlf 55 55 33 33."
    private const val gittFullmaktReplacement = "Du har gitt fullmakt til <fullmektig> og du blir nå representert av vedkommende hos Nav. Se Fullmakter på nav.no for mer informasjon. Har du ikke opprettet en fullmakt ta kontakt med Nav på tlf 55 55 33 33."

    override fun ruleApplies(text: String): Boolean {
        return opprettetFullmakt.matches(text)
            || avsluttetFullmakt.matches(text)
            || endretFullmakt.matches(text)
            || gittFullmakt.matches(text)
    }

    override fun applyRule(text: String): String {
        return when (text) {
            in opprettetFullmakt -> opprettetFullmaktReplacement
            in avsluttetFullmakt -> avsluttetFullmaktReplacement
            in endretFullmakt -> endretFullmaktReplacement
            in gittFullmakt -> gittFullmaktReplacement
            else -> text
        }
    }

    private operator fun Regex.contains(text: String) = this.matches(text)
}
