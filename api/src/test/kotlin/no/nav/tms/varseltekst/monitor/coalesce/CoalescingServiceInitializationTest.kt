package no.nav.tms.varseltekst.monitor.coalesce

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import no.nav.tms.varseltekst.monitor.coalesce.rules.*
import no.nav.tms.varseltekst.monitor.setup.LocalPostgresDatabase
import no.nav.tms.varseltekst.monitor.setup.deleteCoalescingBackLog
import no.nav.tms.varseltekst.monitor.setup.deleteCoalescingRule
import no.nav.tms.varseltekst.monitor.setup.deleteTekst
import no.nav.tms.varseltekst.monitor.varsel.insertTekst
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class CoalescingServiceInitializationTest {

    private val database = LocalPostgresDatabase.migratedDb()

    private val coalescingRepository = CoalescingRepository(database)
    private val backlogRepository = BacklogRepository(database)

    @AfterEach
    fun cleanup() {
        database.run {
            deleteCoalescingBackLog()
            deleteCoalescingRule()
        }
    }

    @Test
    fun `Creates rules in db on initialization`() {
        val rules = listOf(
            NumberCensorRule,
            GreetingCensorRule
        )

        CoalescingService.uninitialized(coalescingRepository, backlogRepository, rules).initialize()

        val ruleDtos = coalescingRepository.getCoalescingRules()

        validateNames(rules, ruleDtos)
        validateDescriptions(rules, ruleDtos)
    }

    @ParameterizedTest
    @CsvSource(
        "WEB_TEKST|Some text|1",
        "WEB_TEKST|Some text,Other text|2",
        "SMS_TEKST|_|0",
        "SMS_TEKST|Some text|1",
        "SMS_TEKST|Some text,Other text|2",
        "EPOST_TITTEL|_|0",
        "EPOST_TITTEL|Some text|1",
        "EPOST_TITTEL|Some text,Other text|2",
        "EPOST_TEKST|_|0",
        "EPOST_TEKST|Some text|1",
        "EPOST_TEKST|Some text,Other text|2",
        delimiter = '|'
    )
    fun `Creates entries in backlog for existing texts when a new rule is added`(table: TekstTable, texts: String, expected: Int) {
        database.run {
            deleteCoalescingBackLog()
            deleteTekst(table)
        }

        val rules = listOf(
            NumberCensorRule,
        )

        insertTexts(table, texts)

        CoalescingService.uninitialized(coalescingRepository, backlogRepository, rules).initialize()

        val numberInBacklog = database.countBackLog(table)

        numberInBacklog shouldBe expected
    }

    private fun validateNames(rules: List<CoalescingRule>, result: List<RuleDto>) {
        val ruleNames = rules.map { it.name }

        val resultNames = result.map { it.name }

        resultNames shouldContainExactly ruleNames
    }

    private fun validateDescriptions(rules: List<CoalescingRule>, result: List<RuleDto>) {
        val ruleDescription = rules.map { it.description }

        val resultDescription = result.map { it.description }

        resultDescription shouldContainExactly ruleDescription
    }

    private fun insertTexts(table: TekstTable, texts: String) {
        val textList = parseList(texts)

        textList.forEach { text ->
            database.insertTekst(table, text)
        }
    }

    private fun parseList(listString: String): List<String> {
        return if (listString == "_") {
            emptyList()
        } else {
            listString.split(",")
        }
    }
}
