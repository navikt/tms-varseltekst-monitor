package no.nav.tms.varseltekst.monitor.config

import no.nav.tms.varseltekst.monitor.coalesce.BacklogRepository
import no.nav.tms.varseltekst.monitor.coalesce.CoalescingBacklogJob
import no.nav.tms.varseltekst.monitor.coalesce.CoalescingRepository
import no.nav.tms.varseltekst.monitor.coalesce.CoalescingService
import no.nav.tms.varseltekst.monitor.coalesce.rules.*
import no.nav.tms.varseltekst.monitor.database.Database
import no.nav.tms.varseltekst.monitor.varsel.VarselRepository
import no.nav.tms.varseltekst.monitor.varsel.VarselSink

class ApplicationContext {

    val environment = Environment()
    val database: Database = PostgresDatabase(environment)

    private val coalescingRepository = CoalescingRepository(database)
    private val backlogRepository = BacklogRepository(database)
    private val varselRepository = VarselRepository(database)

    private lateinit var coalescingService: CoalescingService
    lateinit var coalescingBacklogJob: CoalescingBacklogJob

    val varselSink: VarselSink by lazy { VarselSink(coalescingService, varselRepository) }

    fun initCoalescingService() {
        val rulesList = listOf(
            DayOfWeekDateTimeRule,
            DokumentTittelRule,
            InntektsmeldingRule,
            UtvidetSykepengerRule,
            FullmaktNavnRule
        )

        coalescingService = CoalescingService.initialize(coalescingRepository, backlogRepository, rulesList)
        coalescingBacklogJob = CoalescingBacklogJob(coalescingRepository, backlogRepository, coalescingService)
    }
}

