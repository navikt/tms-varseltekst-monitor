package no.nav.tms.varseltekst.monitor.coalesce

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import kotliquery.queryOf
import no.nav.tms.varseltekst.monitor.coalesce.rules.*
import no.nav.tms.varseltekst.monitor.setup.*
import no.nav.tms.varseltekst.monitor.varsel.TestVarsel
import no.nav.tms.varseltekst.monitor.varsel.VarselOversikt
import no.nav.tms.varseltekst.monitor.varsel.VarselRepository
import no.nav.tms.varseltekst.monitor.varsel.selectVarsel
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class CoalescingBacklogJobTest {

    private val database = LocalPostgresDatabase.migratedDb()


    private val numberCensorRule: CoalescingRuleWrapper
    private val greetingCensorRule: CoalescingRuleWrapper
    private val coalescingService: CoalescingService

    private val backlogRepository = BacklogRepository(database)
    private val coalescingRepository = CoalescingRepository(database)

    init {
        numberCensorRule = createInDb(NumberCensorRule)
        greetingCensorRule = createInDb(GreetingCensorRule)

        coalescingService = CoalescingService.initialized(listOf(numberCensorRule, greetingCensorRule))
    }

    @AfterEach
    fun cleanUp() {
        database.deleteFromDb()
    }

    @AfterAll
    fun finalCleanUp() {
        database.run {
            deleteFromDb()
            deleteCoalescingRule()
        }
    }

    val textWithNumber = "This is a text 123."
    val textWithGreeting = "Hello, Name Namesson! This is a text."
    val textWithWithNumberAndGreeting = "Hello, Name Namesson! This is mambo number 5!"
    val textWithNeither = "It is (possibly) Sunday! This is a text."

    @Test
    fun `Processes backlog and coalesces texts where applicable`() {
        val varsel = TestVarsel.createVarsel(
            webTekst = textWithNumber,
            smsTekst = textWithGreeting,
            epostTittel = textWithNeither,
            epostTekst = null,
        )

        createInDb(varsel)
        generateBacklog(numberCensorRule)

        val backlogJob = CoalescingBacklogJob(coalescingRepository, backlogRepository, coalescingService)

        runBlocking {
            backlogJob.start()
            backlogJob.job.join() // Wait for completion
        }

        val updatedVarsel = getVarsel(varsel.eventId)

        updatedVarsel.webTekst shouldNotBe varsel.webTekst
        updatedVarsel.webTekst shouldBe "This is a text ***."
        updatedVarsel.smsTekst shouldBe varsel.smsTekst
        updatedVarsel.epostTittel shouldBe varsel.epostTittel
        updatedVarsel.epostTekst shouldBe varsel.epostTekst
    }

    private fun createInDb(varsel: VarselOversikt) {
        VarselRepository(database).persistVarsel(varsel)
    }

    private fun createInDb(rule: CoalescingRule): CoalescingRuleWrapper {
        val ruleDto = database.insertRule(rule)

        return CoalescingRuleWrapper(
            definition = rule,
            dto = ruleDto
        )
    }

    private fun generateBacklog(rule: CoalescingRuleWrapper) {
        TekstTable.values().forEach {
            database.insertIntoBacklog(rule.definition, it)
        }
    }

    private fun getVarsel(eventId: String): VarselOversikt {
        return database.selectVarsel(eventId)
    }

    private fun insertBacklogQuery(table: TekstTable) = """
        insert into coalescing_backlog(tekst_table, rule_id, tekst_id)
            select '$table', rule.id, tekst.id from $table as tekst
                join coalescing_rule as rule on rule.name = :rule
    """

    private fun Database.insertIntoBacklog(rule: CoalescingRule, table: TekstTable) = update {
        queryOf(
            insertBacklogQuery(table),
            mapOf("rule" to rule.name)
        )
    }


    private fun Database.deleteFromDb() {
        deleteCoalescingHistoryWebTekst()
        deleteCoalescingHistorySmsTekst()
        deleteCoalescingHistoryEpostTittel()
        deleteCoalescingHistoryEpostTekst()
        deleteCoalescingBackLog()
        deleteVarsel()
        deleteWebTekst()
        deleteSmsTekst()
        deleteEpostTittel()
        deleteEpostTekst()
    }
}
