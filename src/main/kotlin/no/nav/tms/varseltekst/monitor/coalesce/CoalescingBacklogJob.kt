package no.nav.tms.varseltekst.monitor.coalesce

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.tms.varseltekst.monitor.util.PeriodicJob
import kotlin.time.Duration.Companion.milliseconds

class CoalescingBacklogJob(
    private val coalescingRepository: CoalescingRepository,
    private val backlogRepository: BacklogRepository,
    private val coalescingService: CoalescingService
): PeriodicJob(100.milliseconds) {

    private val log = KotlinLogging.logger {}

    override val job = initializeJob {
        processCoalescingBacklog()
    }

    private suspend fun processCoalescingBacklog() {
        val nextEntry = backlogRepository.getNextBacklogEntry()

        if (nextEntry == null) {
            log.info { "Ferdig med prosessering av sammenslåings-backlog. Stopper periodisk jobb." }
            stop()
        } else {
            processBacklogEntry(nextEntry)
        }
    }

    private fun processBacklogEntry(backlogEntry: BacklogEntry) {
        val varselTekst = coalescingRepository.selectTekst(backlogEntry.tekstTable, backlogEntry.tekstId)

        val coalescingResult = coalescingService.coalesce(varselTekst.tekst, backlogEntry.ruleId)

        if (coalescingResult.isCoalesced) {
            coalescingRepository.updateTekst(backlogEntry.ruleId, varselTekst, coalescingResult.finalTekst)
        }

        coalescingRepository.deleteBacklogEntry(backlogEntry.id)
    }
}

data class BacklogEntry(
    val id: Int,
    val tekstTable: TekstTable,
    val ruleId: Int,
    val tekstId: Int
)

data class VarselTekst(
    val table: TekstTable,
    val tekst: String,
    val id: Int
)
