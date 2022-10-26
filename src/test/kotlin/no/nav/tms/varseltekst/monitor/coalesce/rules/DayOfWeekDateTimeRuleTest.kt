package no.nav.tms.varseltekst.monitor.coalesce.rules

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

internal class DayOfWeekDateTimeRuleTest {

    private val dateTimeRule = DayOfWeekDateTimeRule

    @Test
    fun `Matcher passende tekst`() {
        val tekst = "Vi minner om at du har et videomøte tirsdag 1. januar kl. 12:00"

        dateTimeRule.ruleApplies(tekst) shouldBe true
    }

    @Test
    fun `Matcher ikke annen tekst`() {
        val tekst = "Vi minner om at du har et videomøte"

        dateTimeRule.ruleApplies(tekst) shouldBe false
    }

    @Test
    fun `Bytter ut passende tekst`() {
        val tekst = "Vi minner om at du har et videomøte tirsdag 1. januar kl. 12:00"

        val coalescedTekst = dateTimeRule.applyRule(tekst)

        coalescedTekst shouldNotBe tekst
        coalescedTekst shouldBe "Vi minner om at du har et videomøte <tidspunkt>"
    }

    @Test
    fun `Bytter ikke ut annen tekst`() {
        val tekst = "Vi minner om at du har et videomøte"

        dateTimeRule.applyRule(tekst) shouldBe tekst
    }
}
