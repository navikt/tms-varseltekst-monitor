package no.nav.tms.varseltekst.monitor.coalesce.rules

object FullmaktNavnRule: CoalescingRule {
    override val description = "Navn i varsler om fullmakt"

    private val fullmakt = "[fF]ullmakt".toRegex()

    private val gittFullmakt = "(Du har gitt fullmakt til )(.*)( og du blir nå representert av vedkommende hos NAV)(.*)".toRegex()
    private val mottattFullmakt = "(Det ble opprettet en fullmakt og du kan nå representere )(.*)(\\. Se Dine fullmakter for mer informasjon)(.*)".toRegex()
    private val avsluttGittFullmakt = "(Du har avsluttet fullmakten du tidligere har gitt til )(.*)(\\. Se Dine fullmakter for mer informasjon)(.*)".toRegex()
    private val avsluttMottattFullmakt = "(Fullmakt du har fått fra )(.*)( ble avsluttet)(.*)".toRegex()
    private val avsluttGittLovrettet = "(Fullmakten som du har gitt til )(.*)( er opphørt av NAV)(.*)".toRegex()
    private val avsluttMottattLovrettet = "(Fullmakt for )(.*)( er avsluttet)(.*)".toRegex()

    private val replacementFullmektig = "$1<fullmektig>$3$4"
    private val replacementRepresentert = "$1<representert>$3$4"

    override fun ruleApplies(text: String) = text.contains(fullmakt)


    override fun applyRule(text: String): String {
        return when (text) {
            in gittFullmakt -> text.replace(gittFullmakt, replacementFullmektig)
            in mottattFullmakt -> text.replace(mottattFullmakt, replacementRepresentert)
            in avsluttGittFullmakt -> text.replace(avsluttGittFullmakt, replacementFullmektig)
            in avsluttMottattFullmakt -> text.replace(avsluttMottattFullmakt, replacementRepresentert)
            in avsluttGittLovrettet -> text.replace(avsluttGittLovrettet, replacementFullmektig)
            in avsluttMottattLovrettet -> text.replace(avsluttMottattLovrettet, replacementRepresentert)
            else -> text
        }
    }

    private operator fun Regex.contains(text: String) = this.matches(text)
}
