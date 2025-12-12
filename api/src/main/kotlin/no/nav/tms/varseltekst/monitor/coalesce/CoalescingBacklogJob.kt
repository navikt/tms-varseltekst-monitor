package no.nav.tms.varseltekst.monitor.coalesce

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.tms.common.logging.TeamLogs
import no.nav.tms.common.util.scheduling.PeriodicJob
import java.time.Duration
import kotlin.time.measureTime

class CoalescingBacklogJob(
    private val coalescingRepository: CoalescingRepository,
    private val backlogRepository: BacklogRepository,
    private val coalescingService: CoalescingService,
    private val batchSize: Int = 500
): PeriodicJob(Duration.ofMillis(10)) {

    private val log = KotlinLogging.logger {}
    private val teamLog = TeamLogs.logger { }

    override val job = initializeJob {
        processCoalescingBacklog()
    }

    private suspend fun processCoalescingBacklog() {
        val nextEntries = backlogRepository.getNextBacklogEntries(batchSize)

        if (nextEntries.isEmpty()) {
            log.info { "Ferdig med prosessering av sammenslåings-backlog. Stopper periodisk jobb." }
            stop()
        } else {
            nextEntries.forEach(::processBacklogEntry)
        }
    }

    private fun processBacklogEntry(backlogEntry: BacklogEntry) {

        log.info { "Forsøker å vaske data i tabell [${backlogEntry.tekstTable}] med id [${backlogEntry.tekstId}] etter regel med id [${backlogEntry.ruleId}]" }

        measureTime {
            val varselTekst = coalescingRepository.selectTekst(backlogEntry.tekstTable, backlogEntry.tekstId)

            val coalescingResult = coalescingService.coalesce(varselTekst.tekst, backlogEntry.ruleId)

            if (coalescingResult.isCoalesced) {
                coalescingRepository.updateTekst(backlogEntry.ruleId, varselTekst, coalescingResult.finalTekst)
                log.info { "Vasket data i tabell [${backlogEntry.tekstTable}] med id [${backlogEntry.tekstId}] etter regel med id [${backlogEntry.ruleId}]" }
                teamLog.info { "Vasket data med tekst \"${varselTekst.tekst}\" etter regel med id [${backlogEntry.ruleId}], resultalt: \"${coalescingResult.finalTekst}\"." }
            } else {
                log.info { "Ingen endringer gjort for tekst med id [${varselTekst.id}]" }
                teamLog.info { "Ingen endringer gjort for tekst \"${varselTekst.tekst}\"]" }
            }

            coalescingRepository.deleteBacklogEntry(backlogEntry.id)
        }.let {
            log.info {
                "Vasking av tekst: { tabell: ${backlogEntry.tekstTable}, id: ${backlogEntry.tekstId}, regelId: ${backlogEntry.ruleId} } tok ${it.inWholeMilliseconds} ms"
            }
        }
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
