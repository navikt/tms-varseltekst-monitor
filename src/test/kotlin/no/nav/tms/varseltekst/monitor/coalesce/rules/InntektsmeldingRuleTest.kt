package no.nav.tms.varseltekst.monitor.coalesce.rules

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class InntektsmeldingRuleTest {

    @Test
    fun `Matcher passende tekst`() {
        val tekst = "Vi mangler inntektsmeldingen fra Hendig Test-Lokasjon for sykefraværet som startet 1. januar 2023."

        InntektsmeldingRule.ruleApplies(tekst) shouldBe true
    }

    @Test
    fun `Matcher ikke annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Brev om trekt 18 års søknad. Les brevet i dokumentarkivet. Hvis du ikke åpner og leser dokumentet, vil vi sende det til deg i posten."

        InntektsmeldingRule.ruleApplies(tekst) shouldBe false
    }

    @Test
    fun `Bytter ut passende tekst`() {
        val tekst = "Vi mangler inntektsmeldingen fra Min Lille Karamellfabrikk for sykefraværet som startet 10. mars 2023."

        val expected = "Vi mangler inntektsmeldingen fra <arbeidssted> for sykefraværet som startet <dato>."

        val coalescedTekst = InntektsmeldingRule.applyRule(tekst)

        coalescedTekst shouldNotBe tekst
        coalescedTekst shouldBe expected
    }

    @Test
    fun `Bytter ikke ut annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Noe helt annet i 2022. Les brevet i mine-saker."

        InntektsmeldingRule.applyRule(tekst) shouldBe tekst
    }
}
