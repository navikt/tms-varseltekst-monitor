package no.nav.tms.varseltekst.monitor.coalesce.rules

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class SykepengerStatusRuleTest {
    @Test
    fun `Matcher passende tekst`() {
        val variant1 = "Status i saken din om sykepenger: Vi venter på inntektsmelding fra Aktivitetsskolen Ammerud."
        val variant2 = "Status i saken din om sykepenger: Vi mangler fortsatt inntektsmelding fra Gamle Oslo Barnevernstjeneste og har sendt en påminnelse til arbeidsgiveren din om dette.Når vi får den kan vi begynne å behandle søknaden din."

        SykepengerStatusRule.ruleApplies(variant1) shouldBe true
        SykepengerStatusRule.ruleApplies(variant2) shouldBe true
    }

    @Test
    fun `Matcher ikke annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Brev om trekt 18 års søknad. Les brevet i dokumentarkivet. Hvis du ikke åpner og leser dokumentet, vil vi sende det til deg i posten."

        SykepengerStatusRule.ruleApplies(tekst) shouldBe false
    }

    @Test
    fun `Bytter ut passende tekst`() {
        val variant1 = "Status i saken din om sykepenger: Vi venter på inntektsmelding fra Aktivitetsskolen Ammerud."
        val variant2 = "Status i saken din om sykepenger: Vi mangler fortsatt inntektsmelding fra Gamle Oslo Barnevernstjeneste og har sendt en påminnelse til arbeidsgiveren din om dette.Når vi får den kan vi begynne å behandle søknaden din."

        val expected1 = "Status i saken din om sykepenger: Vi venter på inntektsmelding fra <arbeidssted>."
        val expected2 = "Status i saken din om sykepenger: Vi mangler fortsatt inntektsmelding fra <arbeidssted> og har sendt en påminnelse til arbeidsgiveren din om dette.Når vi får den kan vi begynne å behandle søknaden din."

        val coalescedTekst1 = SykepengerStatusRule.applyRule(variant1)
        val coalescedTekst2 = SykepengerStatusRule.applyRule(variant2)

        coalescedTekst1 shouldNotBe variant1
        coalescedTekst1 shouldBe expected1

        coalescedTekst2 shouldNotBe variant2
        coalescedTekst2 shouldBe expected2
    }

    @Test
    fun `Bytter ikke ut annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Noe helt annet i 2022. Les brevet i mine-saker."

        SykepengerStatusRule.applyRule(tekst) shouldBe tekst
    }
}
