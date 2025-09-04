package no.nav.tms.varseltekst.monitor

import no.nav.tms.kafka.application.KafkaApplication
import no.nav.tms.varseltekst.monitor.coalesce.BacklogRepository
import no.nav.tms.varseltekst.monitor.coalesce.CoalescingBacklogJob
import no.nav.tms.varseltekst.monitor.coalesce.CoalescingRepository
import no.nav.tms.varseltekst.monitor.coalesce.CoalescingService
import no.nav.tms.varseltekst.monitor.coalesce.rules.*
import no.nav.tms.varseltekst.monitor.setup.*
import no.nav.tms.varseltekst.monitor.varsel.VarselOpprettetSubscriber
import no.nav.tms.varseltekst.monitor.varsel.VarselRepository
import no.nav.tms.varseltekst.monitor.varseltekst.VarseltekstRepository

fun main() {
    val environment = Environment()
    val database: Database = PostgresDatabase(environment)

    val coalescingRepository = CoalescingRepository(database)
    val backlogRepository = BacklogRepository(database)

    val coalesingRules = listOf(
        ArbeidsmarkedtiltakRule,
        DayOfWeekDateTimeRule,
        DokumentTittelGjelderRule,
        DokumentTittelRule,
        FullmaktNavnRule,
        InntektsmeldingRule,
        KontonummerEndretTidspunktRule,
        SykefravaerOrgRule,
        UtvidetSykepengerRule
    )

    val coalescingService = CoalescingService.uninitialized(
        coalescingRepository = coalescingRepository,
        backlogRepository = backlogRepository,
        rules = coalesingRules
    )

    val coalescingBacklogJob = CoalescingBacklogJob(
        coalescingRepository = coalescingRepository,
        backlogRepository = backlogRepository,
        coalescingService = coalescingService
    )

    KafkaApplication.build {
        ktorModule {
            varseltekstMonitor(
                varseltekstRepository = VarseltekstRepository(database)
            )
        }

        kafkaConfig {
            readTopics(environment.varselTopic)
            groupId = environment.groupId
        }

        subscribers(
            VarselOpprettetSubscriber(coalescingService, VarselRepository(database))
        )

        onReady {
            Flyway.runFlywayMigrations(environment)
            coalescingService.initialize()
            coalescingBacklogJob.start()
        }
    }.start()
}
