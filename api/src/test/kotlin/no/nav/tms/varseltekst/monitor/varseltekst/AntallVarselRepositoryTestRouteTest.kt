package no.nav.tms.varseltekst.monitor.varseltekst

import io.kotest.matchers.collections.shouldBeSortedDescendingBy
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
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

class AntallVarselRepositoryTestRouteTest {
    private val database = LocalPostgresDatabase.migratedDb()

    private val varselRepository = VarselRepository(database)
    private val varseltekstRepository = VarseltekstRepository(database)

    @AfterEach
    fun reset() {
        database.clearAllTables()
    }

    @Test
    fun `teller antall varseltekster per varseltype`() {
        fillDb(10, "Hallo!", varseltype = "oppgave")
        fillDb(3, "Hallo!", varseltype = "innboks")
        fillDb(5, "Hallo!", varseltype = "innboks")

        val telteAntall = tellAntallVarseltekster(Teksttype.WebTekst)

        telteAntall.first { it.varseltype == "oppgave" }.antall shouldBe 10
        telteAntall.first { it.varseltype == "innboks" }.antall shouldBe 8

        telteAntall.sumOf { it.antall } shouldBe 18
    }

    @Test
    fun `teller antall varseltekster per produsent`() {
        fillDb(3, "Hallo!", produsent = Produsent("team-tim", "appen"))
        fillDb(5, "Hallo!", produsent = Produsent("team-tim", "appto"))
        fillDb(13, "Hallo!", produsent = Produsent("team-annet", "app-api"))

        val telteAntall = tellAntallVarseltekster(Teksttype.WebTekst)

        telteAntall.first { it.produsent.appnavn == "appen" }.antall shouldBe 3
        telteAntall.first { it.produsent.appnavn == "appto" }.antall shouldBe 5
        telteAntall.first { it.produsent.appnavn == "app-api" }.antall shouldBe 13

        telteAntall.sumOf { it.antall } shouldBe 21
    }

    @Test
    fun `teller antall varseltekster opprettet etter dato`() {
        fillDb(7, "Ny!", tidspunkt = LocalDateTime.now())
        fillDb(5, "Gammel!", tidspunkt = LocalDateTime.now().minusDays(10))

        val fiveDaysAgo = LocalDate.now().minusDays(5)

        val telteAntall = tellAntallVarseltekster(Teksttype.WebTekst, startDato = fiveDaysAgo)

        telteAntall.first { it.tekst == "Ny!" }.antall shouldBe 7
        telteAntall.firstOrNull { it.tekst == "Gammel!" }.shouldBeNull()

        telteAntall.sumOf { it.antall } shouldBe 7
    }

    @Test
    fun `teller antall varseltekster opprettet før dato`() {
        fillDb(7, "Ny!", tidspunkt = LocalDateTime.now())
        fillDb(5, "Gammel!", tidspunkt = LocalDateTime.now().minusDays(10))

        val fiveDaysAgo = LocalDate.now().minusDays(5)

        val telteAntall = tellAntallVarseltekster(Teksttype.WebTekst, sluttDato = fiveDaysAgo)

        telteAntall.firstOrNull { it.tekst == "Ny!" }.shouldBeNull()
        telteAntall.first { it.tekst == "Gammel!" }.antall shouldBe 5

        telteAntall.sumOf { it.antall } shouldBe 5
    }

    @Test
    fun `teller antall varseltekster av type`() {
        fillDb(3, "Beskjed!", varseltype = "beskjed")
        fillDb(5, "Oppgave!", varseltype = "oppgave")
        fillDb(7, "Innboks!", varseltype = "innboks")

        val telteAntall = tellAntallVarseltekster(Teksttype.WebTekst, varseltype = "innboks")

        telteAntall.sumOf { it.antall } shouldBe 7
    }

    @Test
    fun `kan telle uten standardtekst for eksterne tekster`() {
        fillDb(3, smsSendt = true, smsTekst = "En sms med egendefinert tekst!")
        fillDb(7, smsSendt = true, smsTekst = null)

        val telteAntall = tellAntallVarseltekster(Teksttype.SmsTekst, inkluderStandardtekster = false)

        telteAntall.sumOf { it.antall } shouldBe 3
    }

    @Test
    fun `kan telle med standardtekst for eksterne tekster`() {
        fillDb(3, smsSendt = true, smsTekst = "En sms med egendefinert tekst!")
        fillDb(7, smsSendt = true, smsTekst = null)

        val telteAntall = tellAntallVarseltekster(Teksttype.SmsTekst, inkluderStandardtekster = true)

        telteAntall.sumOf { it.antall } shouldBe 10
    }

    @Test
    fun `data kommer sortert i synkende antall`() {
        fillDb(3, "Tekst 1", varseltype = "oppgave")
        fillDb(23, "Tekst 2", varseltype = "oppgave")
        fillDb(17, "Tekst 3", varseltype = "innboks")
        fillDb(11, "Tekst 4", varseltype = "beskjed")
        fillDb(29, "Tekst 5", varseltype = "oppgave")

        val telteAntall = tellAntallVarseltekster(Teksttype.WebTekst)
        telteAntall.shouldBeSortedDescendingBy { it.antall }
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

    fun tellAntallVarseltekster(
        teksttype: Teksttype,
        varseltype: String? = null,
        startDato: LocalDate? = null,
        sluttDato: LocalDate? = null,
        inkluderStandardtekster: Boolean = false
    ) = varseltekstRepository.tellAntallVarseltekster(
        teksttype,
        varseltype,
        startDato,
        sluttDato,
        inkluderStandardtekster
    )
}
