package no.nav.tms.varseltekst.monitor.coalesce.rules

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FullmaktNavnRule2Test {
    @Test
    fun `Matcher passende tekst`() {
        val opprettetFullmaktTekst = "Det ble opprettet en fullmakt og du kan nå representere PER NESER TRET. Se Fullmakter på nav.no for mer informasjon."
        val avsluttetFullmaktTekst = "Du har avsluttet fullmakten du tidligere har gitt til FULL MEKTIG. Se Fullmakter på nav.no for mer informasjon. Har du ikke avsluttet en fullmakt ta kontakt med Nav på tlf 55 55 33 33."
        val endretFullmaktTekst = "Du har endret fullmakten du tidligere har gitt til FULL MEKTIG. Se Fullmakter på nav.no for mer informasjon. Har du ikke endret en fullmakt ta kontakt med Nav på tlf 55 55 33 33."
        val gittFullmaktTekst = "Du har gitt fullmakt til FULL MEKTIG og du blir nå representert av vedkommende hos Nav. Se Fullmakter på nav.no for mer informasjon. Har du ikke opprettet en fullmakt ta kontakt med Nav på tlf 55 55 33 33."

        FullmaktNavnRule2.ruleApplies(opprettetFullmaktTekst) shouldBe true
        FullmaktNavnRule2.ruleApplies(avsluttetFullmaktTekst) shouldBe true
        FullmaktNavnRule2.ruleApplies(endretFullmaktTekst) shouldBe true
        FullmaktNavnRule2.ruleApplies(gittFullmaktTekst) shouldBe true
    }

    @Test
    fun `Matcher ikke annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Brev om trekt 18 års søknad. Les brevet i dokumentarkivet. Hvis du ikke åpner og leser dokumentet, vil vi sende det til deg i posten."

        FullmaktNavnRule2.ruleApplies(tekst) shouldBe false
    }

    @Test
    fun `Bytter ut passende tekst`() {
        val opprettetFullmaktTekst = "Det ble opprettet en fullmakt og du kan nå representere PER NESER TRET. Se Fullmakter på nav.no for mer informasjon."
        val avsluttetFullmaktTekst = "Du har avsluttet fullmakten du tidligere har gitt til FULL MEKTIG. Se Fullmakter på nav.no for mer informasjon. Har du ikke avsluttet en fullmakt ta kontakt med Nav på tlf 55 55 33 33."
        val endretFullmaktTekst = "Du har endret fullmakten du tidligere har gitt til FULL MEKTIG. Se Fullmakter på nav.no for mer informasjon. Har du ikke endret en fullmakt ta kontakt med Nav på tlf 55 55 33 33."
        val gittFullmaktTekst = "Du har gitt fullmakt til FULL MEKTIG og du blir nå representert av vedkommende hos Nav. Se Fullmakter på nav.no for mer informasjon. Har du ikke opprettet en fullmakt ta kontakt med Nav på tlf 55 55 33 33."

        val opprettetFullmaktExpected = "Det ble opprettet en fullmakt og du kan nå representere <representert>. Se Fullmakter på nav.no for mer informasjon."
        val avsluttetFullmaktExpected = "Du har avsluttet fullmakten du tidligere har gitt til <fullmektig>. Se Fullmakter på nav.no for mer informasjon. Har du ikke avsluttet en fullmakt ta kontakt med Nav på tlf 55 55 33 33."
        val endretFullmaktExpected = "Du har endret fullmakten du tidligere har gitt til <fullmektig>. Se Fullmakter på nav.no for mer informasjon. Har du ikke endret en fullmakt ta kontakt med Nav på tlf 55 55 33 33."
        val gittFullmaktExpected = "Du har gitt fullmakt til <fullmektig> og du blir nå representert av vedkommende hos Nav. Se Fullmakter på nav.no for mer informasjon. Har du ikke opprettet en fullmakt ta kontakt med Nav på tlf 55 55 33 33."

        FullmaktNavnRule2.applyRule(opprettetFullmaktTekst) shouldBe opprettetFullmaktExpected
        FullmaktNavnRule2.applyRule(avsluttetFullmaktTekst) shouldBe avsluttetFullmaktExpected
        FullmaktNavnRule2.applyRule(endretFullmaktTekst) shouldBe endretFullmaktExpected
        FullmaktNavnRule2.applyRule(gittFullmaktTekst) shouldBe gittFullmaktExpected
    }

    @Test
    fun `Bytter ikke ut annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Noe helt annet i 3033. Les brevet i mine-saker."

        FullmaktNavnRule2.applyRule(tekst) shouldBe tekst
    }
}
