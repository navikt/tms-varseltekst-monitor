package no.nav.tms.varseltekst.monitor.coalesce.rules

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ArbeidsmarkedtiltakRule2Test {

    @Test
    fun `Matcher passende tekst`() {
        val soktInnTekst = "Du er søkt inn på arbeidsmarkedstiltaket Kurs hos Storskolen."
        val faattPlassTekst = "Du har fått plass på arbeidsmarkedstiltaket: Opplæring hos Norges Kommune"
        val mottattUtkastTekst = "Du har mottatt et utkast til søknad på arbeidsmarkedstiltaket Opplæring hos Kompetansemølla AS. Svar på spørsmålet her."

        ArbeidsmarkedtiltakRuleExt.ruleApplies(soktInnTekst) shouldBe true
        ArbeidsmarkedtiltakRuleExt.ruleApplies(faattPlassTekst) shouldBe true
        ArbeidsmarkedtiltakRuleExt.ruleApplies(mottattUtkastTekst) shouldBe true
    }

    @Test
    fun `Matcher ikke annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Brev om trekt 18 års søknad. Les brevet i dokumentarkivet. Hvis du ikke åpner og leser dokumentet, vil vi sende det til deg i posten."

        ArbeidsmarkedtiltakRuleExt.ruleApplies(tekst) shouldBe false
    }

    @Test
    fun `Bytter ut passende tekst`() {
        val utkastTekst = "Du har mottatt et utkast til påmelding på arbeidsmarkedstiltaket: Digitalt jobbsøkerkurs hos Søte Fabrikker AS. Svar på spørsmålet her."
        val paameldtTekst = "Du er meldt på arbeidsmarkedstiltaket: Digitalt jobbsøkerkurs hos Søte Fabrikker AS."
        val endringTekst = "Ny endring på arbeidsmarkedstiltaket: Digitalt jobbsøkerkurs hos Søte Fabrikker AS."

        val utkastExpected = "Du har mottatt et utkast til påmelding på arbeidsmarkedstiltaket: <tiltak hos arbeidssted>. Svar på spørsmålet her."
        val paameldtExpected = "Du er meldt på arbeidsmarkedstiltaket: <tiltak hos arbeidssted>."
        val endringExpected = "Ny endring på arbeidsmarkedstiltaket: <tiltak hos arbeidssted>."

        ArbeidsmarkedtiltakRuleExt.applyRule(utkastTekst) shouldBe utkastExpected
        ArbeidsmarkedtiltakRuleExt.applyRule(paameldtTekst) shouldBe paameldtExpected
        ArbeidsmarkedtiltakRuleExt.applyRule(endringTekst) shouldBe endringExpected
    }

    @Test
    fun `Bytter ikke ut annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Noe helt annet i 2022. Les brevet i mine-saker."

        ArbeidsmarkedtiltakRuleExt.applyRule(tekst) shouldBe tekst
    }
}
