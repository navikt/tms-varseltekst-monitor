package no.nav.tms.varseltekst.monitor

import no.nav.tms.kafka.application.AppHealth
import no.nav.tms.kafka.application.KafkaApplication
import no.nav.tms.varseltekst.monitor.coalesce.BacklogRepository
import no.nav.tms.varseltekst.monitor.coalesce.CoalescingBacklogJob
import no.nav.tms.varseltekst.monitor.coalesce.CoalescingRepository
import no.nav.tms.varseltekst.monitor.coalesce.CoalescingService
import no.nav.tms.varseltekst.monitor.coalesce.rules.*
import no.nav.tms.varseltekst.monitor.setup.*
import no.nav.tms.varseltekst.monitor.varsel.VarselOpprettetSubscriber
import no.nav.tms.varseltekst.monitor.varsel.VarselRepository

fun main() {
    val environment = Environment()
    val database: Database = PostgresDatabase(environment)

    val coalescingRepository = CoalescingRepository(database)
    val backlogRepository = BacklogRepository(database)

    val coalesingRules = listOf(
        DayOfWeekDateTimeRule,
        DokumentTittelRule,
        InntektsmeldingRule,
        UtvidetSykepengerRule,
        FullmaktNavnRule
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
        kafkaConfig {
            readTopics(environment.varselTopic)
            groupId = environment.groupId
        }

        subscribers(
            VarselOpprettetSubscriber(coalescingService, VarselRepository(database))
        )

        onStartup {
            Flyway.runFlywayMigrations(environment)
            coalescingService.initialize()
        }

        onReady {
            coalescingBacklogJob.start()
        }

        healthCheck("BacklogJob") {
            if (coalescingBacklogJob.job.isActive) {
                AppHealth.Healthy
            } else {
                AppHealth.Unhealthy
            }
        }
    }.start()
}
