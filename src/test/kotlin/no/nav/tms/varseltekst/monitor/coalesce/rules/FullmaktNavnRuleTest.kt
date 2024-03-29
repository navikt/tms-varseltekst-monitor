package no.nav.tms.varseltekst.monitor.coalesce.rules

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FullmaktNavnRuleTest {
    @Test
    fun `Matcher passende tekst`() {
        val forventetMottatt = "Det ble opprettet en fullmakt og du kan nå representere LYKKELIG REPRESENTERT. Se Dine fullmakter for mer informasjon."
        val forventetGitt = "Du har gitt fullmakt til IVRIG REPRESENTANT og du blir nå representert av vedkommende hos NAV. Se Dine fullmakter for mer informasjon. Har du ikke opprettet en fullmakt ta kontakt på tlf."
        val forventetMottattAvsluttet = "Fullmakt du har fått fra LYKKELIG REPRESENTERT ble avsluttet. Se Dine fullmakter for mer informasjon."
        val forventetGittAvsluttet = "Du har avsluttet fullmakten du tidligere har gitt til IVRIG REPRESENTANT. Se Dine fullmakter for mer informasjon. Har du ikke avsluttet en fullmakt ta kontakt på tlf."
        val annenPassende = "Tilfeldig tekst med nøkkelord 'fullmakt'"

        FullmaktNavnRule.ruleApplies(forventetMottatt) shouldBe true
        FullmaktNavnRule.ruleApplies(forventetGitt) shouldBe true
        FullmaktNavnRule.ruleApplies(forventetMottattAvsluttet) shouldBe true
        FullmaktNavnRule.ruleApplies(forventetGittAvsluttet) shouldBe true
        FullmaktNavnRule.ruleApplies(annenPassende) shouldBe true
    }

    @Test
    fun `Matcher ikke annen tekst`() {
        val tekst = "Helt annen tekst uten nøkkelordet."

        FullmaktNavnRule.ruleApplies(tekst) shouldBe false
    }

    @Test
    fun `Bytter ut passende tekst`() {
        val mottatt = "Det ble opprettet en fullmakt og du kan nå representere LYKKELIG REPRESENTERT. Se Dine fullmakter for mer informasjon."
        val gitt = "Du har gitt fullmakt til IVRIG REPRESENTANT og du blir nå representert av vedkommende hos NAV. Se Dine fullmakter for mer informasjon. Har du ikke opprettet en fullmakt ta kontakt på tlf."
        val mottattAvsluttet = "Fullmakt du har fått fra LYKKELIG REPRESENTERT ble avsluttet. Se Dine fullmakter for mer informasjon."
        val gittAvsluttet = "Du har avsluttet fullmakten du tidligere har gitt til IVRIG REPRESENTANT. Se Dine fullmakter for mer informasjon. Har du ikke avsluttet en fullmakt ta kontakt på tlf."
        val avsluttGittLovrettet = "Fullmakten som du har gitt til IVRIG REPRESENTANT er opphørt av NAV. Du må ta kontakt med vedkommende for årsak til dette. Du kan opprette en ny fullmakt med en annen fullmektig."
        val avsluttMottattLovrettet = "Fullmakt for LYKKELIG REPRESENTERT er avsluttet, jf. forvaltningsloven § 12 andre ledd. Grunnen er at du er ansatt i NAV."

        val expectedMottatt = "Det ble opprettet en fullmakt og du kan nå representere <representert>. Se Dine fullmakter for mer informasjon."
        val expectedGitt = "Du har gitt fullmakt til <fullmektig> og du blir nå representert av vedkommende hos NAV. Se Dine fullmakter for mer informasjon. Har du ikke opprettet en fullmakt ta kontakt på tlf."
        val expectedMottattAvsluttet = "Fullmakt du har fått fra <representert> ble avsluttet. Se Dine fullmakter for mer informasjon."
        val expectedGittAvsluttet = "Du har avsluttet fullmakten du tidligere har gitt til <fullmektig>. Se Dine fullmakter for mer informasjon. Har du ikke avsluttet en fullmakt ta kontakt på tlf."
        val expetedAvsluttGittLovrettet = "Fullmakten som du har gitt til <fullmektig> er opphørt av NAV. Du må ta kontakt med vedkommende for årsak til dette. Du kan opprette en ny fullmakt med en annen fullmektig."
        val expetedAvsluttMottattLovrettet = "Fullmakt for <representert> er avsluttet, jf. forvaltningsloven § 12 andre ledd. Grunnen er at du er ansatt i NAV."

        FullmaktNavnRule.applyRule(mottatt) shouldBe expectedMottatt
        FullmaktNavnRule.applyRule(gitt) shouldBe expectedGitt
        FullmaktNavnRule.applyRule(mottattAvsluttet) shouldBe expectedMottattAvsluttet
        FullmaktNavnRule.applyRule(gittAvsluttet) shouldBe expectedGittAvsluttet
        FullmaktNavnRule.applyRule(avsluttGittLovrettet) shouldBe expetedAvsluttGittLovrettet
        FullmaktNavnRule.applyRule(avsluttMottattLovrettet) shouldBe expetedAvsluttMottattLovrettet
    }

    @Test
    fun `Bytter ikke ut annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Noe helt annet i 2022 om fullmakt. Les brevet i mine-saker."

        FullmaktNavnRule.applyRule(tekst) shouldBe tekst
    }
}
