package no.nav.tms.varseltekst.monitor.coalesce.rules

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class DokumentTittelRuleTest {

    @Test
    fun `Matcher passende tekst`() {
        val tekst = "Du har fått et brev som du må lese: Brev om trekt 18 års søknad. Les brevet i dokumentarkivet. Hvis du ikke åpner og leser dokumentet, vil vi sende det til deg i posten."

        DokumentTittelRule.ruleApplies(tekst) shouldBe true
    }

    @Test
    fun `Matcher ikke annen tekst`() {
        val tekst = "Vi minner om at du har et videomøte tirsdag 1. januar kl. 12:00"

        DokumentTittelRule.ruleApplies(tekst) shouldBe false
    }

    @Test
    fun `Bytter ut passende tekst`() {
        val tekst1 = "Du har fått et brev som du må lese: Bekreftelse på trukket søknad. Les brevet i dokumentarkivet. Hvis du ikke åpner og leser dokumentet, vil vi sende det til deg i posten."
        val tekst2 = "Du har fått et brev som du må lese: Brev om trekt 18 års søknad. Les brevet i mine-saker. Hvis du ikke åpner og leser dokumentet, vil vi sende det til deg i posten."
        val tekst3 = "Du har fått et brev som du må lese: Noe helt annet i 2022. Les brevet i mine-saker."
        val tekst4 = "Du har fått et vedtak som du må lese: Vedtak om noen ting. Les vedtaket i mine-saker."

        val expected1 = "Du har fått et brev som du må lese: <tittel>. Les brevet i dokumentarkivet. Hvis du ikke åpner og leser dokumentet, vil vi sende det til deg i posten."
        val expected2 = "Du har fått et brev som du må lese: <tittel>. Les brevet i mine-saker. Hvis du ikke åpner og leser dokumentet, vil vi sende det til deg i posten."
        val expected3 = "Du har fått et brev som du må lese: <tittel>. Les brevet i mine-saker."
        val expected4 = "Du har fått et vedtak som du må lese: <tittel>. Les vedtaket i mine-saker."

        val coalescedTekst1 = DokumentTittelRule.applyRule(tekst1)
        val coalescedTekst2 = DokumentTittelRule.applyRule(tekst2)
        val coalescedTekst3 = DokumentTittelRule.applyRule(tekst3)
        val coalescedTekst4 = DokumentTittelRule.applyRule(tekst4)

        coalescedTekst1 shouldNotBe tekst1
        coalescedTekst1 shouldBe expected1

        coalescedTekst2 shouldNotBe tekst2
        coalescedTekst2 shouldBe expected2

        coalescedTekst3 shouldNotBe tekst3
        coalescedTekst3 shouldBe expected3

        coalescedTekst4 shouldNotBe tekst4
        coalescedTekst4 shouldBe expected4
    }

    @Test
    fun `Bytter ikke ut annen tekst`() {
        val tekst = "Vi minner om at du har et videomøte"

        DokumentTittelRule.applyRule(tekst) shouldBe tekst
    }
}
