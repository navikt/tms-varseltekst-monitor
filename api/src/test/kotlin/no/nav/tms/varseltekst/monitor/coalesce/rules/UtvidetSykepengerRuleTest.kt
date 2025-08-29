package no.nav.tms.varseltekst.monitor.coalesce.rules

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class UtvidetSykepengerRuleTest {

    @Test
    fun `Matcher passende tekst`() {
        val tekst = "Særdeles Ukjent Arbeidsplass har søkt om utvidet støtte fra NAV angående sykepenger til deg."

        UtvidetSykepengerRule.ruleApplies(tekst) shouldBe true
    }

    @Test
    fun `Matcher ikke annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Brev om trekt 18 års søknad. Les brevet i dokumentarkivet. Hvis du ikke åpner og leser dokumentet, vil vi sende det til deg i posten."

        UtvidetSykepengerRule.ruleApplies(tekst) shouldBe false
    }

    @Test
    fun `Bytter ut passende tekst`() {
        val tekst = "Enda Mindre Kjent Arbeidsplass har søkt om utvidet støtte fra NAV angående sykepenger til deg."

        val expected = "<arbeidssted> har søkt om utvidet støtte fra NAV angående sykepenger til deg."

        val coalescedTekst = UtvidetSykepengerRule.applyRule(tekst)

        coalescedTekst shouldNotBe tekst
        coalescedTekst shouldBe expected
    }

    @Test
    fun `Bytter ikke ut annen tekst`() {
        val tekst = "Vi minner om at du har et videomøte"

        UtvidetSykepengerRule.applyRule(tekst) shouldBe tekst
    }
}
