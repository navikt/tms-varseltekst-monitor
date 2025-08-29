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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class TotaltAntallRouteTest {
    private val database = LocalPostgresDatabase.migratedDb()

    private val varselRepository = VarselRepository(database)
    private val varseltekstRepository = VarseltekstRepository(database)

    @AfterEach
    fun reset() {
        database.clearAllTables()
    }

    @Test
    fun `teller totalt antall varseltekster`() = testApi {
        fillDb(10, "Hei!")
        fillDb(5, "Hallo!")

        val telteAntall = client.get("/antall/${Teksttype.WebTekst}/totalt")
            .json()


        telteAntall.first { it["tekst"].asText() == "Hei!" }["antall"].asInt() shouldBe 10
        telteAntall.first { it["tekst"].asText() == "Hallo!" }["antall"].asInt() shouldBe 5

        telteAntall.sumOf { it["antall"].asInt() } shouldBe 15
    }

    @Test
    fun `teller totalt antall varseltekster opprettet etter dato`() = testApi {
        fillDb(7, "Ny!", tidspunkt = LocalDateTime.now())
        fillDb(5, "Gammel!", tidspunkt = LocalDateTime.now().minusDays(10))

        val fiveDaysAgo = LocalDate.now().minusDays(5)

        val telteAntall = client.get("/antall/${Teksttype.WebTekst}/totalt?startDato=$fiveDaysAgo")
            .json()

        telteAntall.first { it["tekst"].asText() == "Ny!" }["antall"].asInt() shouldBe 7
        telteAntall.firstOrNull { it["tekst"].asText() == "Gammel!" }.shouldBeNull()

        telteAntall.sumOf { it["antall"].asInt() } shouldBe 7
    }

    @Test
    fun `teller totalt antall varseltekster opprettet før dato`() = testApi {
        fillDb(7, "Ny!", tidspunkt = LocalDateTime.now())
        fillDb(5, "Gammel!", tidspunkt = LocalDateTime.now().minusDays(10))

        val fiveDaysAgo = LocalDate.now().minusDays(5)

        val telteAntall = client.get("/antall/${Teksttype.WebTekst}/totalt?sluttDato=$fiveDaysAgo")
            .json()

        telteAntall.firstOrNull() { it["tekst"].asText() == "Ny!" }.shouldBeNull()
        telteAntall.first() { it["tekst"].asText() == "Gammel!" }["antall"].asInt() shouldBe 5

        telteAntall.sumOf { it["antall"].asInt() } shouldBe 5
    }

    @Test
    fun `teller totalt antall varseltekster av type`() = testApi {
        fillDb(3, "Beskjed!", varseltype = "beskjed")
        fillDb(5, "Oppgave!", varseltype = "oppgave")
        fillDb(7, "Innboks!", varseltype = "innboks")

        val telteAntall = client.get("/antall/${Teksttype.WebTekst}/totalt?varselType=innboks")
            .json()

        telteAntall.sumOf { it["antall"].asInt() } shouldBe 7
    }

    @Test
    fun `teller uten standardtekst som default`() = testApi {
        fillDb(3, smsSendt = true, smsTekst = "En sms med egendefinert tekst!")
        fillDb(7, smsSendt = true, smsTekst = null)

        val telteAntall = client.get("/antall/${Teksttype.SmsTekst}/totalt")
            .json()

        telteAntall.sumOf { it["antall"].asInt() } shouldBe 3
    }

    @Test
    fun `teller med standardtekst hvis forespurt`() = testApi {
        fillDb(3, smsSendt = true, smsTekst = "En sms med egendefinert tekst!")
        fillDb(7, smsSendt = true, smsTekst = null)

        val telteAntall = client.get("/antall/${Teksttype.SmsTekst}/totalt?standardtekster=true")
            .json()

        telteAntall.sumOf { it["antall"].asInt() } shouldBe 10
    }

    @Test
    fun `data kommer sortert i synkende antall`() = testApi {
        fillDb(3, "Tekst 1")
        fillDb(23, "Tekst 2")
        fillDb(17, "Tekst 3")
        fillDb(11, "Tekst 4")
        fillDb(29, "Tekst 5")

        val telteAntall = client.get("/antall/${Teksttype.WebTekst}/totalt")
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
            varseltekstApi(
                varseltekstRepository,
            )
        }

        block()
    }

    private val objectMapper = jacksonObjectMapper()

    private suspend fun HttpResponse.json() = bodyAsText().let { objectMapper.readTree(it) }
}
