package no.nav.tms.varseltekst.monitor.coalesce.rules

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ArbeidsmarkedtiltakRuleTest {

    @Test
    fun `Matcher passende tekst`() {
        val utkastTekst = "Du har mottatt et utkast til påmelding på arbeidsmarkedstiltaket: Digitalt jobbsøkerkurs hos Søte Fabrikker AS. Svar på spørsmålet her."
        val paameldtTekst = "Du er meldt på arbeidsmarkedstiltaket: Digitalt jobbsøkerkurs hos Søte Fabrikker AS."
        val endringTekst = "Ny endring på arbeidsmarkedstiltaket: Digitalt jobbsøkerkurs hos Søte Fabrikker AS."

        ArbeidsmarkedtiltakRule.ruleApplies(utkastTekst) shouldBe true
        ArbeidsmarkedtiltakRule.ruleApplies(paameldtTekst) shouldBe true
        ArbeidsmarkedtiltakRule.ruleApplies(endringTekst) shouldBe true
    }

    @Test
    fun `Matcher ikke annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Brev om trekt 18 års søknad. Les brevet i dokumentarkivet. Hvis du ikke åpner og leser dokumentet, vil vi sende det til deg i posten."

        ArbeidsmarkedtiltakRule.ruleApplies(tekst) shouldBe false
    }

    @Test
    fun `Bytter ut passende tekst`() {
        val utkastTekst = "Du har mottatt et utkast til påmelding på arbeidsmarkedstiltaket: Digitalt jobbsøkerkurs hos Søte Fabrikker AS. Svar på spørsmålet her."
        val paameldtTekst = "Du er meldt på arbeidsmarkedstiltaket: Digitalt jobbsøkerkurs hos Søte Fabrikker AS."
        val endringTekst = "Ny endring på arbeidsmarkedstiltaket: Digitalt jobbsøkerkurs hos Søte Fabrikker AS."

        val utkastExpected = "Du har mottatt et utkast til påmelding på arbeidsmarkedstiltaket: <tiltak hos arbeidssted>. Svar på spørsmålet her."
        val paameldtExpected = "Du er meldt på arbeidsmarkedstiltaket: <tiltak hos arbeidssted>."
        val endringExpected = "Ny endring på arbeidsmarkedstiltaket: <tiltak hos arbeidssted>."

        ArbeidsmarkedtiltakRule.applyRule(utkastTekst) shouldBe utkastExpected
        ArbeidsmarkedtiltakRule.applyRule(paameldtTekst) shouldBe paameldtExpected
        ArbeidsmarkedtiltakRule.applyRule(endringTekst) shouldBe endringExpected
    }

    @Test
    fun `Bytter ikke ut annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Noe helt annet i 2022. Les brevet i mine-saker."

        ArbeidsmarkedtiltakRule.applyRule(tekst) shouldBe tekst
    }
}
