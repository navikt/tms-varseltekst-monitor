package no.nav.tms.varseltekst.monitor.coalesce.rules

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class DokumentTittelGjelderRuleTest {

    @Test
    fun `Matcher passende tekst`() {
        val tekst = "Du har fått et vedtak som gjelder Vedtak om endret barnetrygd - barn 18 år. Les vedtaket i dokumentarkivet. Hvis du ikke åpner og leser dokumentet, vil vi sende det til deg i posten."

        DokumentTittelGjelderRule.ruleApplies(tekst) shouldBe true
    }

    @Test
    fun `Matcher ikke annen tekst`() {
        val tekst = "Vi minner om at du har et videomøte tirsdag 1. januar kl. 12:00"

        DokumentTittelGjelderRule.ruleApplies(tekst) shouldBe false
    }

    @Test
    fun `Bytter ut passende tekst`() {
        val tekst1 = "Du har fått et vedtak som gjelder Vedtak om endret barnetrygd - barn 18 år. Les vedtaket i dokumentarkivet. Hvis du ikke åpner og leser dokumentet, vil vi sende det til deg i posten."
        val tekst2 = "Du har fått et vedtak som gjelder Vedtak om innvilgelse av engangsstønad. Les vedtaket i dokumentarkivet."
        val tekst3 = "Du har fått et vedtak som gjelder Vedtak om arbeidsavklaringspenger. Les vedtaket i mine saker."

        val expected1 = "Du har fått et vedtak som gjelder <tittel>. Les vedtaket i dokumentarkivet. Hvis du ikke åpner og leser dokumentet, vil vi sende det til deg i posten."
        val expected2 = "Du har fått et vedtak som gjelder <tittel>. Les vedtaket i dokumentarkivet."
        val expected3 = "Du har fått et vedtak som gjelder <tittel>. Les vedtaket i mine saker."

        val coalescedTekst1 = DokumentTittelGjelderRule.applyRule(tekst1)
        val coalescedTekst2 = DokumentTittelGjelderRule.applyRule(tekst2)
        val coalescedTekst3 = DokumentTittelGjelderRule.applyRule(tekst3)

        coalescedTekst1 shouldNotBe tekst1
        coalescedTekst1 shouldBe expected1

        coalescedTekst2 shouldNotBe tekst2
        coalescedTekst2 shouldBe expected2

        coalescedTekst3 shouldNotBe tekst3
        coalescedTekst3 shouldBe expected3

    }

    @Test
    fun `Bytter ikke ut annen tekst`() {
        val tekst = "Vi minner om at du har et videomøte"

        DokumentTittelGjelderRule.applyRule(tekst) shouldBe tekst
    }
}
