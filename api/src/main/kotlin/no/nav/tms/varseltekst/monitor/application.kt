package no.nav.tms.varseltekst.monitor

import no.nav.tms.common.postgres.Postgres
import no.nav.tms.kafka.application.KafkaApplication
import no.nav.tms.varseltekst.monitor.coalesce.BacklogRepository
import no.nav.tms.varseltekst.monitor.coalesce.CoalescingBacklogJob
import no.nav.tms.varseltekst.monitor.coalesce.CoalescingRepository
import no.nav.tms.varseltekst.monitor.coalesce.CoalescingService
import no.nav.tms.varseltekst.monitor.coalesce.rules.*
import no.nav.tms.varseltekst.monitor.setup.Environment
import no.nav.tms.varseltekst.monitor.varsel.VarselOpprettetSubscriber
import no.nav.tms.varseltekst.monitor.varsel.VarselRepository
import no.nav.tms.varseltekst.monitor.varseltekst.VarseltekstRequestProcessor
import no.nav.tms.varseltekst.monitor.varseltekst.VarseltekstRepository
import org.flywaydb.core.Flyway

fun main() {
    val environment = Environment()
    val database = Postgres.connectToJdbcUrl(environment.jdbcUrl)

    val coalescingRepository = CoalescingRepository(database)
    val backlogRepository = BacklogRepository(database)

    val coalesingRules = listOf(
        ArbeidsmarkedtiltakRule,
        ArbeidsmarkedtiltakRule2,
        ArbeidsmarkedtiltakRule3,
        DayOfWeekDateTimeRule,
        DokumentTittelGjelderRule,
        DokumentTittelRule,
        FullmaktNavnRule,
        FullmaktNavnRule2,
        InntektsmeldingRule,
        KontonummerEndretTidspunktRule,
        StillingHosArbeidsstedRule,
        SykefravaerOrgRule,
        SykepengerStatusRule,
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

    val queryHandler = VarseltekstRequestProcessor(
        VarseltekstRepository(database)
    )

    KafkaApplication.build {
        ktorModule {
            varseltekstMonitor(
                queryHandler = queryHandler
            )
        }

        kafkaConfig {
            readTopics(environment.varselTopic)
            groupId = environment.groupId
        }

        subscribers(
            VarselOpprettetSubscriber(coalescingService, VarselRepository(database))
        )

        onStartup {
            Flyway.configure()
                .dataSource(database.dataSource)
                .load()
                .migrate()

            coalescingService.initialize()
        }

        onReady {
            coalescingBacklogJob.start()
        }
    }.start()
}
