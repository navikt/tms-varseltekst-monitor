package no.nav.tms.varseltekst.monitor.coalesce.rules

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class SykefravaerOrgRuleTest {

    @Test
    fun `Matcher passende tekst`() {
        val tekst = "ARBEIDSSTED SOM SØKER DEKNING AV SYKEPENGER har søkt om at NAV dekker sykepenger fra første dag av sykefraværet ditt."

        SykefravaerOrgRule.ruleApplies(tekst) shouldBe true
    }

    @Test
    fun `Matcher ikke annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Brev om trekt 18 års søknad. Les brevet i dokumentarkivet. Hvis du ikke åpner og leser dokumentet, vil vi sende det til deg i posten."

        SykefravaerOrgRule.ruleApplies(tekst) shouldBe false
    }

    @Test
    fun `Bytter ut passende tekst`() {
        val tekst = "ARBEIDSSTED SOM SØKER DEKNING AV SYKEPENGER har søkt om at NAV dekker sykepenger fra første dag av sykefraværet ditt."

        val expected = "<arbeidssted> har søkt om at NAV dekker sykepenger fra første dag av sykefraværet ditt."

        val coalescedTekst = SykefravaerOrgRule.applyRule(tekst)

        coalescedTekst shouldNotBe tekst
        coalescedTekst shouldBe expected
    }

    @Test
    fun `Bytter ikke ut annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Noe helt annet i 2022. Les brevet i mine-saker."

        SykefravaerOrgRule.applyRule(tekst) shouldBe tekst
    }
}
