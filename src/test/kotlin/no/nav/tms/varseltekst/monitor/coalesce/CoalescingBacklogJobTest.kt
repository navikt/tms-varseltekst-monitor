package no.nav.tms.varseltekst.monitor.coalesce

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import no.nav.tms.varseltekst.monitor.coalesce.rules.*
import no.nav.tms.varseltekst.monitor.config.*
import no.nav.tms.varseltekst.monitor.varsel.TestVarsel
import no.nav.tms.varseltekst.monitor.varsel.VarselOversikt
import no.nav.tms.varseltekst.monitor.varsel.VarselRepository
import no.nav.tms.varseltekst.monitor.varsel.selectVarsel
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.sql.Connection

internal class CoalescingBacklogJobTest {

    private val database = LocalPostgresDatabase.migratedDb()


    private val numberCensorRule: CoalescingRuleWrapper
    private val greetingCensorRule: CoalescingRuleWrapper
    private val coalescingService: CoalescingService

    private val coalescingRepository = CoalescingRepository(database)

    init {
        numberCensorRule = createInDb(NumberCensorRule)
        greetingCensorRule = createInDb(GreetingCensorRule)

        coalescingService = CoalescingService(listOf(numberCensorRule, greetingCensorRule))
    }

    @AfterEach
    fun cleanUp() {
        database.dbQuery {
            deleteFromDb()
        }
    }

    @AfterAll
    fun finalCleanUp() {
        database.dbQuery {
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

        val backlogJob = CoalescingBacklogJob(coalescingRepository, coalescingService)

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

    @Test
    fun `Applies multiple rules in order of creation`() {

    }

    private fun createInDb(varsel: VarselOversikt) {
        VarselRepository(database).persistVarsel(varsel)
    }

    private fun createInDb(rule: CoalescingRule): CoalescingRuleWrapper {
        val ruleDto = database.dbQuery {
            insertRule(rule)
        }

        return CoalescingRuleWrapper(
            definition = rule,
            dto = ruleDto
        )
    }

    private fun generateBacklog(rule: CoalescingRuleWrapper) {
        database.dbQuery {
            TekstTable.values().forEach {
                insertIntoBacklog(rule.definition, it)
            }
        }
    }

    private fun getVarsel(eventId: String): VarselOversikt {
        return database.dbQuery {
            selectVarsel(eventId)
        }
    }

    private fun Connection.deleteFromDb() {
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
