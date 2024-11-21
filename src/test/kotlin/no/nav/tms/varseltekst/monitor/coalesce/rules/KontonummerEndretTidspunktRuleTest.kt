package no.nav.tms.varseltekst.monitor.coalesce.rules

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class KontonummerEndretTidspunktRuleTest {

    @Test
    fun `matcher alle tidspunkt med relevant tekst`() {
        (0..23).forEach { hour ->
            val hourString = hour.toString().padStart(2, '0')

            val tekst = "Hei! Kontonummeret ditt hos NAV ble endret 01.01. kl. $hourString:00. Hvis det ikke var deg som endret, må du logge deg inn på NAV for å rette kontonummeret. Trenger du hjelp, kan du ringe oss på 55 55 33 33 kl. 09:00–15:00. Hilsen NAV."

            KontonummerEndretTidspunktRule.applyRule(tekst) shouldBe "Hei! Kontonummeret ditt hos NAV ble endret <tidspunkt>. Hvis det ikke var deg som endret, må du logge deg inn på NAV for å rette kontonummeret. Trenger du hjelp, kan du ringe oss på 55 55 33 33 kl. 09:00–15:00. Hilsen NAV."
            KontonummerEndretTidspunktRule.ruleApplies(tekst) shouldBe true

        }

        (0..59).forEach { minute ->
            val minuteString = minute.toString().padStart(2, '0')

            val tekst = "Hei! Kontonummeret ditt hos NAV ble endret 01.01. kl. 00:$minuteString. Hvis det ikke var deg som endret, må du logge deg inn på NAV for å rette kontonummeret. Trenger du hjelp, kan du ringe oss på 55 55 33 33 kl. 09:00–15:00. Hilsen NAV."

            KontonummerEndretTidspunktRule.ruleApplies(tekst) shouldBe true
            KontonummerEndretTidspunktRule.applyRule(tekst) shouldBe "Hei! Kontonummeret ditt hos NAV ble endret <tidspunkt>. Hvis det ikke var deg som endret, må du logge deg inn på NAV for å rette kontonummeret. Trenger du hjelp, kan du ringe oss på 55 55 33 33 kl. 09:00–15:00. Hilsen NAV."
        }
    }

    @Test
    fun `matcher alle datoer med relevant tekst`() {
        (1..12).forEach { month ->
            val tekst = "Hei! Kontonummeret ditt hos NAV ble endret 01.$month. kl. 00:00. Hvis det ikke var deg som endret, må du logge deg inn på NAV for å rette kontonummeret. Trenger du hjelp, kan du ringe oss på 55 55 33 33 kl. 09:00–15:00. Hilsen NAV."

            KontonummerEndretTidspunktRule.applyRule(tekst) shouldBe "Hei! Kontonummeret ditt hos NAV ble endret <tidspunkt>. Hvis det ikke var deg som endret, må du logge deg inn på NAV for å rette kontonummeret. Trenger du hjelp, kan du ringe oss på 55 55 33 33 kl. 09:00–15:00. Hilsen NAV."
            KontonummerEndretTidspunktRule.ruleApplies(tekst) shouldBe true

        }

        (1..31).forEach { day ->
            val tekst = "Hei! Kontonummeret ditt hos NAV ble endret $day.01. kl. 00:00. Hvis det ikke var deg som endret, må du logge deg inn på NAV for å rette kontonummeret. Trenger du hjelp, kan du ringe oss på 55 55 33 33 kl. 09:00–15:00. Hilsen NAV."

            KontonummerEndretTidspunktRule.ruleApplies(tekst) shouldBe true
            KontonummerEndretTidspunktRule.applyRule(tekst) shouldBe "Hei! Kontonummeret ditt hos NAV ble endret <tidspunkt>. Hvis det ikke var deg som endret, må du logge deg inn på NAV for å rette kontonummeret. Trenger du hjelp, kan du ringe oss på 55 55 33 33 kl. 09:00–15:00. Hilsen NAV."
        }
    }

    @Test
    fun `matcher ikke annen tekst`() {
        val tekst = "Du har fått et brev som du må lese: Noe helt annet i 2022. Les brevet i mine-saker."

        KontonummerEndretTidspunktRule.ruleApplies(tekst) shouldBe false
        KontonummerEndretTidspunktRule.applyRule(tekst) shouldBe tekst
    }
}
