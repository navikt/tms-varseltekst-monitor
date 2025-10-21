package no.nav.tms.varseltekst.monitor.coalesce.rules

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ArbeidsmarkedtiltakRule2Test {

    @Test
    fun `Matcher passende tekst`() {
        val soktInnTekst = "Du er søkt inn på arbeidsmarkedstiltaket Kurs hos Storskolen."
        val faattPlassTekst = "Du har fått plass på arbeidsmarkedstiltaket: Opplæring hos Norges Kommune"
        val mottattUtkastTekst = "Du har mottatt et utkast til søknad på arbeidsmarkedstiltaket Opplæring hos Kompetansemølla AS. Svar på spørsmålet her."

        ArbeidsmarkedtiltakRule2.ruleApplies(soktInnTekst) shouldBe true
        ArbeidsmarkedtiltakRule2.ruleApplies(faattPlassTekst) shouldBe true
        ArbeidsmarkedtiltakRule2.ruleApplies(mottattUtkastTekst) shouldBe true
    }

    @Test
    fun `Matcher ikke annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Brev om trekt 18 års søknad. Les brevet i dokumentarkivet. Hvis du ikke åpner og leser dokumentet, vil vi sende det til deg i posten."

        ArbeidsmarkedtiltakRule2.ruleApplies(tekst) shouldBe false
    }

    @Test
    fun `Bytter ut passende tekst`() {
        val soktInnTekst = "Du er søkt inn på arbeidsmarkedstiltaket Kurs hos Storskolen."
        val faattPlassTekst = "Du har fått plass på arbeidsmarkedstiltaket: Opplæring hos Norges Kommune"
        val mottattUtkastTekst = "Du har mottatt et utkast til søknad på arbeidsmarkedstiltaket Opplæring hos Kompetansemølla AS. Svar på spørsmålet her."

        val soktInnExpected = "Du er søkt inn på arbeidsmarkedstiltaket <tiltak hos arbeidssted>."
        val faattPlassExpected = "Du har fått plass på arbeidsmarkedstiltaket: <tiltak hos arbeidssted>"
        val mottattUtkastExpected = "Du har mottatt et utkast til søknad på arbeidsmarkedstiltaket <tiltak hos arbeidssted>. Svar på spørsmålet her."

        ArbeidsmarkedtiltakRule2.applyRule(soktInnTekst) shouldBe soktInnExpected
        ArbeidsmarkedtiltakRule2.applyRule(faattPlassTekst) shouldBe faattPlassExpected
        ArbeidsmarkedtiltakRule2.applyRule(mottattUtkastTekst) shouldBe mottattUtkastExpected
    }

    @Test
    fun `Bytter ikke ut annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Noe helt annet i 2022. Les brevet i mine-saker."

        ArbeidsmarkedtiltakRule2.applyRule(tekst) shouldBe tekst
    }
}
