package no.nav.tms.varseltekst.monitor.coalesce

import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import no.nav.tms.varseltekst.monitor.coalesce.rules.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDateTime

internal class CoalescingServiceTest {

    private val ruleWrappers = createWrapper(
        1 to NumberCensorRule,
        2 to GreetingCensorRule
    )

    private val service = CoalescingService.initialized(ruleWrappers)

    @ParameterizedTest
    @CsvSource(
        "This is a text 123|This is a text ***|true|NumberCensorRule",
        "This is a text 456|This is a text ***|true|NumberCensorRule",
        "Hello, Name Namesson! This is a text.|Hello, <name>! This is a text.|true|GreetingCensorRule",
        "Hello, Piesee McIntosh! This is a text.|Hello, <name>! This is a text.|true|GreetingCensorRule",
        "Hello, Name Namesson Jr! This is mambo number 5.|Hello, <name>! This is mambo number ***.|true|NumberCensorRule,GreetingCensorRule",
        "Hello to you.|Hello to you.|false|_",
        delimiter = '|'
    )
    fun `Applies all rules to given texts`(originalText: String, expectedResult: String, coalesced: Boolean, rulesApplied: String) {
        val coalescingResult = service.coalesce(originalText)

        val rules = parseList(rulesApplied)

        coalescingResult.isCoalesced shouldBe coalesced
        coalescingResult.rulesApplied.map { it.name } shouldContainAll rules
        coalescingResult.originalTekst shouldBe originalText
        coalescingResult.finalTekst shouldBe expectedResult
    }

    @ParameterizedTest
    @CsvSource(
        "This is a text 123|This is a text ***|1|true",
        "This is a text 123|This is a text 123|2|false",
        "Hello, Name Namesson! This is a text.|Hello, Name Namesson! This is a text.|1|false",
        "Hello, Name Namesson! This is a text.|Hello, <name>! This is a text.|2|true",
        "Hello, Name Namesson Jr! This is mambo number 5.|Hello, Name Namesson Jr! This is mambo number ***.|1|true",
        "Hello, Name Namesson Jr! This is mambo number 5.|Hello, <name>! This is mambo number 5.|2|true",
        "Hello to you.|Hello to you.|1|false",
        "Hello to you.|Hello to you.|2|false",
        delimiter = '|'
    )
    fun `Applies specified rule to text`(originalText: String, expectedResult: String, ruleId: Int, coalesced: Boolean) {
        val coalescingResult = service.coalesce(originalText, ruleId)

        coalescingResult.isCoalesced shouldBe coalesced
        coalescingResult.originalTekst shouldBe originalText
        coalescingResult.finalTekst shouldBe expectedResult
    }

    private fun parseList(listString: String): List<String> {
        return if (listString == "_") {
            emptyList()
        } else {
            listString.split(",")
        }
    }

    private fun createWrapper(vararg ruleWithId: Pair<Int, CoalescingRule>): List<CoalescingRuleWrapper> {
        return ruleWithId.map { (id, rule) ->
            CoalescingRuleWrapper(
                rule,
                RuleDto(id, rule.name, rule.description, LocalDateTime.now())
            )
        }
    }
}
