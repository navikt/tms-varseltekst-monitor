package no.nav.tms.varseltekst.monitor.coalesce.rules

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ArbeidsmarkedtiltakRule3Test {
    @Test
    fun `Matcher passende tekst`() {
        val ingaattTekst = "Din avtale om Inkluderingstilskudd er inngått"
        val godkjenneTekst = "Du må godkjenne en avtale om Arbeidstrening hos HEIA HEIA med oppstartsdato 01.01.2025"
        val avlystTekst = "Avtalen om Inkluderingstilskudd hos VESTRE ØSTLI ble avlyst"
        val sluttdatoTekst = "Sluttdatoen for Midlertidig lønnstilskudd hos MEGAPOLIS AUTO AS er forlenget til 01.01.2026"

        ArbeidsmarkedtiltakRule3.ruleApplies(ingaattTekst) shouldBe true
        ArbeidsmarkedtiltakRule3.ruleApplies(godkjenneTekst) shouldBe true
        ArbeidsmarkedtiltakRule3.ruleApplies(avlystTekst) shouldBe true
        ArbeidsmarkedtiltakRule3.ruleApplies(sluttdatoTekst) shouldBe true
    }

    @Test
    fun `Matcher ikke annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Brev om trekt 18 års søknad. Les brevet i dokumentarkivet. Hvis du ikke åpner og leser dokumentet, vil vi sende det til deg i posten."

        ArbeidsmarkedtiltakRule3.ruleApplies(tekst) shouldBe false
    }

    @Test
    fun `Bytter ut passende tekst`() {
        val ingaattTekst = "Din avtale om Inkluderingstilskudd er inngått"
        val godkjenneTekst = "Du må godkjenne en avtale om Arbeidstrening hos HEIA HEIA med oppstartsdato 01.01.2025"
        val avlystTekst = "Avtalen om Inkluderingstilskudd hos VESTRE ØSTLI ble avlyst"
        val sluttdatoTekst = "Sluttdatoen for Midlertidig lønnstilskudd hos MEGAPOLIS AUTO AS er forlenget til 01.01.2026"

        val ingaattExpected = "Din avtale om <tiltak> er inngått"
        val godkjenneExpected = "Du må godkjenne en avtale om <tiltak hos arbeidssted> med oppstartsdato <dato>"
        val avlystExpected = "Avtalen om <tiltak hos arbeidssted> ble avlyst"
        val sluttdatoExpected = "Sluttdatoen for <tiltak hos arbeidssted> er forlenget til <dato>"

        ArbeidsmarkedtiltakRule3.applyRule(ingaattTekst) shouldBe ingaattExpected
        ArbeidsmarkedtiltakRule3.applyRule(godkjenneTekst) shouldBe godkjenneExpected
        ArbeidsmarkedtiltakRule3.applyRule(avlystTekst) shouldBe avlystExpected
        ArbeidsmarkedtiltakRule3.applyRule(sluttdatoTekst) shouldBe sluttdatoExpected
    }

    @Test
    fun `Bytter ikke ut annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Noe helt annet i 3033. Les brevet i mine-saker."

        ArbeidsmarkedtiltakRule3.applyRule(tekst) shouldBe tekst
    }
}
