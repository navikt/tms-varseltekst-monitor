package no.nav.tms.varseltekst.monitor.varseltekst

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.collections.shouldBeSortedDescendingBy
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import io.ktor.utils.io.*
import no.nav.tms.varseltekst.monitor.setup.LocalPostgresDatabase
import no.nav.tms.varseltekst.monitor.setup.clearAllTables
import no.nav.tms.varseltekst.monitor.varsel.Produsent
import no.nav.tms.varseltekst.monitor.varsel.VarselOversikt
import no.nav.tms.varseltekst.monitor.varsel.VarselRepository
import no.nav.tms.varseltekst.monitor.varseltekstMonitor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class AntallRouteTest {
    private val database = LocalPostgresDatabase.migratedDb()

    private val varselRepository = VarselRepository(database)
    private val varseltekstRepository = VarseltekstRepository(database)

    @AfterEach
    fun reset() {
        database.clearAllTables()
    }

    @Test
    fun `teller antall varseltekster per varseltype`() = testApi {
        fillDb(10, "Hallo!", varseltype = "oppgave")
        fillDb(3, "Hallo!", varseltype = "innboks")
        fillDb(5, "Hallo!", varseltype = "innboks")

        val telteAntall = client.get("/antall/${Teksttype.WebTekst}")
            .json()

        telteAntall.first { it["varseltype"].asText() == "oppgave" }["antall"].asInt() shouldBe 10
        telteAntall.first { it["varseltype"].asText() == "innboks" }["antall"].asInt() shouldBe 8

        telteAntall.sumOf { it["antall"].asInt() } shouldBe 18
    }

    @Test
    fun `teller antall varseltekster per produsent`() = testApi {
        fillDb(3, "Hallo!", produsent = Produsent("team-tim", "appen"))
        fillDb(5, "Hallo!", produsent = Produsent("team-tim", "appto"))
        fillDb(13, "Hallo!", produsent = Produsent("team-annet", "app-api"))

        val telteAntall = client.get("/antall/${Teksttype.WebTekst}")
            .json()

        telteAntall.first { it["produsent"]["appnavn"].asText() == "appen" }["antall"].asInt() shouldBe 3
        telteAntall.first { it["produsent"]["appnavn"].asText() == "appto" }["antall"].asInt() shouldBe 5
        telteAntall.first { it["produsent"]["appnavn"].asText() == "app-api" }["antall"].asInt() shouldBe 13

        telteAntall.sumOf { it["antall"].asInt() } shouldBe 21
    }

    @Test
    fun `teller antall varseltekster opprettet etter dato`() = testApi {
        fillDb(7, "Ny!", tidspunkt = LocalDateTime.now())
        fillDb(5, "Gammel!", tidspunkt = LocalDateTime.now().minusDays(10))

        val fiveDaysAgo = LocalDate.now().minusDays(5)

        val telteAntall = client.get("/antall/${Teksttype.WebTekst}?startDato=$fiveDaysAgo")
            .json()

        telteAntall.first { it["tekst"].asText() == "Ny!" }["antall"].asInt() shouldBe 7
        telteAntall.firstOrNull { it["tekst"].asText() == "Gammel!" }.shouldBeNull()

        telteAntall.sumOf { it["antall"].asInt() } shouldBe 7
    }

    @Test
    fun `teller antall varseltekster opprettet før dato`() = testApi {
        fillDb(7, "Ny!", tidspunkt = LocalDateTime.now())
        fillDb(5, "Gammel!", tidspunkt = LocalDateTime.now().minusDays(10))

        val fiveDaysAgo = LocalDate.now().minusDays(5)

        val telteAntall = client.get("/antall/${Teksttype.WebTekst}?sluttDato=$fiveDaysAgo")
            .json()

        telteAntall.firstOrNull { it["tekst"].asText() == "Ny!" }.shouldBeNull()
        telteAntall.first { it["tekst"].asText() == "Gammel!" }["antall"].asInt() shouldBe 5

        telteAntall.sumOf { it["antall"].asInt() } shouldBe 5
    }

    @Test
    fun `teller antall varseltekster av type`() = testApi {
        fillDb(3, "Beskjed!", varseltype = "beskjed")
        fillDb(5, "Oppgave!", varseltype = "oppgave")
        fillDb(7, "Innboks!", varseltype = "innboks")

        val telteAntall = client.get("/antall/${Teksttype.WebTekst}?varselType=innboks")
            .json()

        telteAntall.sumOf { it["antall"].asInt() } shouldBe 7
    }

    @Test
    fun `teller uten standardtekst som default`() = testApi {
        fillDb(3, smsSendt = true, smsTekst = "En sms med egendefinert tekst!")
        fillDb(7, smsSendt = true, smsTekst = null)

        val telteAntall = client.get("/antall/${Teksttype.SmsTekst}")
            .json()

        telteAntall.sumOf { it["antall"].asInt() } shouldBe 3
    }

    @Test
    fun `teller med standardtekst hvis forespurt`() = testApi {
        fillDb(3, smsSendt = true, smsTekst = "En sms med egendefinert tekst!")
        fillDb(7, smsSendt = true, smsTekst = null)

        val telteAntall = client.get("/antall/${Teksttype.SmsTekst}?standardtekster=true")
            .json()

        telteAntall.sumOf { it["antall"].asInt() } shouldBe 10
    }

    @Test
    fun `data kommer sortert i synkende antall`() = testApi {
        fillDb(3, "Tekst 1", varseltype = "oppgave")
        fillDb(23, "Tekst 2", varseltype = "oppgave")
        fillDb(17, "Tekst 3", varseltype = "innboks")
        fillDb(11, "Tekst 4", varseltype = "beskjed")
        fillDb(29, "Tekst 5", varseltype = "oppgave")

        val telteAntall = client.get("/antall/${Teksttype.WebTekst}")
            .json()

        telteAntall.shouldBeSortedDescendingBy { it["antall"].asInt() }
    }

    private fun fillDb(
        antall: Int,
        webTekst: String = "Hei hallo på min side",
        smsSendt: Boolean = false,
        smsTekst: String? = null,
        epostSendt: Boolean = false,
        epostTekst: String? = null,
        epostTittel: String? = null,
        varseltype: String = "beskjed",
        produsent: Produsent = Produsent("testnamespace", "testapp"),
        tidspunkt: LocalDateTime = LocalDateTime.now()
    ) {
        repeat(antall) {
            VarselOversikt(
                eventId = UUID.randomUUID().toString(),
                eventType = varseltype,
                producerNamespace = produsent.namespace,
                producerAppnavn = produsent.appnavn,
                eksternVarsling = smsSendt || epostSendt,
                preferertKanalSms = smsSendt,
                preferertKanalEpost = epostSendt,
                webTekst = webTekst,
                smsTekst = smsTekst,
                epostTittel = epostTittel,
                epostTekst = epostTekst,
                varseltidspunkt = tidspunkt,
            ).let {
                varselRepository.persistVarsel(it)
            }
        }
    }

    @KtorDsl
    private fun testApi(
        block: suspend ApplicationTestBuilder.() -> Unit
    ) = testApplication {

        application {
            varseltekstMonitor(
                varseltekstRepository,
            )
        }

        block()
    }

    private val objectMapper = jacksonObjectMapper()

    private suspend fun HttpResponse.json() = bodyAsText().let { objectMapper.readTree(it) }
}
