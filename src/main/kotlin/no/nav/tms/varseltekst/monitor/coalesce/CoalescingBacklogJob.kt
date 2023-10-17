package no.nav.tms.varseltekst.monitor.coalesce

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.tms.varseltekst.monitor.util.PeriodicJob
import kotlin.concurrent.timer
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

class CoalescingBacklogJob(
    private val coalescingRepository: CoalescingRepository,
    private val backlogRepository: BacklogRepository,
    private val coalescingService: CoalescingService
): PeriodicJob(100.milliseconds) {

    private val log = KotlinLogging.logger {}
    private val secureLog = KotlinLogging.logger("secureLog")

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

        log.info { "Forsøker å vaske data i tabell [${backlogEntry.tekstTable}] med id [${backlogEntry.tekstId}] etter regel med id [${backlogEntry.ruleId}]" }

        measureTime {
            val varselTekst = coalescingRepository.selectTekst(backlogEntry.tekstTable, backlogEntry.tekstId)

            val coalescingResult = coalescingService.coalesce(varselTekst.tekst, backlogEntry.ruleId)

            if (coalescingResult.isCoalesced) {
                coalescingRepository.updateTekst(backlogEntry.ruleId, varselTekst, coalescingResult.finalTekst)
                log.info { "Vasket data i tabell [${backlogEntry.tekstTable}] med id [${backlogEntry.tekstId}] etter regel med id [${backlogEntry.ruleId}]" }
                secureLog.info { "Vasket data med tekst \"${varselTekst.tekst}\" etter regel med id [${backlogEntry.ruleId}], resultalt: \"${coalescingResult.finalTekst}\"." }
            } else {
                log.info { "Ingen endringer gjort for tekst med id [${varselTekst.id}]" }
                secureLog.info { "Ingen endringer gjort for tekst \"${varselTekst.tekst}\"]" }
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
