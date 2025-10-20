package no.nav.tms.varseltekst.monitor.coalesce.rules

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class StillingHosArbeidsstedRuleTest {
    @Test
    fun `Matcher passende tekst`() {
        val variant1 = "Vi har vurdert at kompetansen din kan passe til stillingen «Assistent» hos «Firma AS». Se stillingen her."
        val variant2 = "Vi har funnet stillingen «Vi søker hjelp!» hos «Businessfabrikken» som kan passe deg. Interessert? Søk via lenka i annonsen."

        StillingHosArbeidsstedRule.ruleApplies(variant1) shouldBe true
        StillingHosArbeidsstedRule.ruleApplies(variant2) shouldBe true
    }

    @Test
    fun `Matcher ikke annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Brev om trekt 18 års søknad. Les brevet i dokumentarkivet. Hvis du ikke åpner og leser dokumentet, vil vi sende det til deg i posten."

        StillingHosArbeidsstedRule.ruleApplies(tekst) shouldBe false
    }

    @Test
    fun `Bytter ut passende tekst`() {
        val variant1 = "Vi har vurdert at kompetansen din kan passe til stillingen «Assistent» hos «Firma AS». Se stillingen her."
        val variant2 = "Vi har funnet stillingen «Vi søker hjelp!» hos «Businessfabrikken» som kan passe deg. Interessert? Søk via lenka i annonsen."

        val expected1 = "Vi har vurdert at kompetansen din kan passe til stillingen <stilling> hos <arbeidssted>. Se stillingen her."
        val expected2 = "Vi har funnet stillingen <stilling> hos <arbeidssted> som kan passe deg. Interessert? Søk via lenka i annonsen."

        val coalescedTekst1 = StillingHosArbeidsstedRule.applyRule(variant1)
        val coalescedTekst2 = StillingHosArbeidsstedRule.applyRule(variant2)

        coalescedTekst1 shouldNotBe variant1
        coalescedTekst1 shouldBe expected1

        coalescedTekst2 shouldNotBe variant2
        coalescedTekst2 shouldBe expected2
    }

    @Test
    fun `Bytter ikke ut annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Noe helt annet i 2022. Les brevet i mine-saker."

        StillingHosArbeidsstedRule.applyRule(tekst) shouldBe tekst
    }
}
