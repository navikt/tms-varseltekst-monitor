package no.nav.tms.varseltekst.monitor.varsel

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import no.nav.tms.kafka.application.MessageBroadcaster
import no.nav.tms.varseltekst.monitor.coalesce.BacklogRepository
import no.nav.tms.varseltekst.monitor.coalesce.CoalescingRepository
import no.nav.tms.varseltekst.monitor.coalesce.CoalescingService
import no.nav.tms.varseltekst.monitor.coalesce.TekstTable.*
import no.nav.tms.varseltekst.monitor.coalesce.rules.CoalescingRule
import no.nav.tms.varseltekst.monitor.coalesce.rules.NumberCensorRule
import no.nav.tms.varseltekst.monitor.setup.LocalPostgresDatabase
import no.nav.tms.varseltekst.monitor.setup.clearAllTables
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class VarselOpprettetSubscriberTest {
    private val database = LocalPostgresDatabase.cleanDb()

    private val varselRepository = VarselRepository(database)
    private val coalescingRepository = CoalescingRepository(database)
    private val backlogRepository = BacklogRepository(database)

    @AfterEach
    fun reset() {
        database.clearAllTables()
    }

    @Test
    fun `leser varsel fra rapid`() {
        val broadcaster = setupBroadcaster()

        val varsel = varselJson(
            type = "beskjed",
            eventId = "123",
            eksternVarsling = true,
            prefererteKanaler = listOf("SMS"),
            tekst = "webTekst",
            smsVarslingstekst = "smsTekst",
            epostVarslingstittel = "epostTittel",
            epostVarslingstekst = "epostTekst"
        )

        broadcaster.broadcastJson(varsel)

        val result = database.selectVarsel("123")

        result.eventId shouldBe "123"
        result.eventType shouldBe "beskjed"
        result.eksternVarsling shouldBe true
        result.preferertKanalSms shouldBe true
        result.preferertKanalEpost shouldBe false
        result.webTekst shouldBe "webTekst"
        result.smsTekst shouldBe "smsTekst"
        result.epostTittel shouldBe "epostTittel"
        result.epostTekst shouldBe "epostTekst"
    }

    @Test
    fun `lagrer unike varseltekster`() {
        val broadcaster = setupBroadcaster()

        val varsel1 = varselJson(
            type = "beskjed",
            eventId = "123",
            eksternVarsling = true,
            tekst = "tekst for web",
            smsVarslingstekst = "smsTekst",
            epostVarslingstittel = "epostTittel",
            epostVarslingstekst = "epostTekst"
        )

        val varsel2 = varselJson(
            type = "beskjed",
            eventId = "456",
            eksternVarsling = true,
            tekst = "annen tekst for web",
            smsVarslingstekst = "smsTekst",
            epostVarslingstittel = "epostTittel",
            epostVarslingstekst = "epostTekst"
        )

        broadcaster.broadcastJson(varsel1)
        broadcaster.broadcastJson(varsel2)

        database.antallTekster(WEB_TEKST) shouldBe 2
        database.antallTekster(SMS_TEKST) shouldBe 1
        database.antallTekster(EPOST_TITTEL) shouldBe 1
        database.antallTekster(EPOST_TEKST) shouldBe 1

        database.getTekster(WEB_TEKST) shouldContainAll listOf("tekst for web", "annen tekst for web")
        database.getTekster(SMS_TEKST) shouldContain "smsTekst"
        database.getTekster(EPOST_TITTEL) shouldContain "epostTittel"
        database.getTekster(EPOST_TEKST) shouldContain "epostTekst"
    }

    @Test
    fun `bruker CoalescingRule til å slå sammen visse tekster`() {
        val broadcaster = setupBroadcaster(NumberCensorRule)

        val varsel1 = varselJson(
            type = "beskjed",
            eventId = "123",
            tekst = "beskjedTekst",
            smsVarslingstekst = "sms-tekst 1"
        )

        val varsel2 = varselJson(
            type = "oppgave",
            eventId = "456",
            tekst = "oppgaveTekst",
            smsVarslingstekst = "sms-tekst 2"
        )

        val varsel3 = varselJson(
            type = "innboks",
            eventId = "456",
            tekst = "innboksTekst",
            smsVarslingstekst = "sms-tekst 3"
        )

        broadcaster.broadcastJson(varsel1)
        broadcaster.broadcastJson(varsel2)
        broadcaster.broadcastJson(varsel3)

        database.antallTekster(WEB_TEKST) shouldBe 3
        database.antallTekster(SMS_TEKST) shouldBe 1

        database.getTekster(SMS_TEKST) shouldContain "sms-tekst ***"
    }

    private fun setupBroadcaster(vararg rules: CoalescingRule) = VarselOpprettetSubscriber(
        coalescingService = CoalescingService.uninitialized(coalescingRepository, backlogRepository, rules.toList()).initialize(),
        varselRepository = varselRepository
    ).let {
        MessageBroadcaster(it)
    }
}
