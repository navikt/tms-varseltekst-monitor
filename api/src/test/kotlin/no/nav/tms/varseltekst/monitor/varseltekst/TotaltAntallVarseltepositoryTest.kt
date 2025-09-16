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

class TotaltAntallVarseltepositoryTest {
    private val database = LocalPostgresDatabase.migratedDb()

    private val varselRepository = VarselRepository(database)
    private val varseltekstRepository = VarseltekstRepository(database)

    @AfterEach
    fun reset() {
        database.clearAllTables()
    }

    @Test
    fun `teller totalt antall varseltekster`() {
        fillDb(10, "Hei!")
        fillDb(5, "Hallo!")

        val telteAntall = tellAntallVarselteksterTotalt(Teksttype.WebTekst)

        telteAntall.permutasjoner.first { it.tekst() == "Hei!" }.antall shouldBe 10
        telteAntall.permutasjoner.first { it.tekst() == "Hallo!" }.antall shouldBe 5

        telteAntall.permutasjoner.sumOf { it.antall } shouldBe 15
    }

    @Test
    fun `teller totalt antall varseltekster opprettet etter dato`() {
        fillDb(7, "Ny!", tidspunkt = LocalDateTime.now())
        fillDb(5, "Gammel!", tidspunkt = LocalDateTime.now().minusDays(10))

        val fiveDaysAgo = LocalDate.now().minusDays(5)

        val telteAntall = tellAntallVarselteksterTotalt(Teksttype.WebTekst, startDato = fiveDaysAgo)

        telteAntall.permutasjoner.first { it.tekst() == "Ny!" }.antall shouldBe 7
        telteAntall.permutasjoner.firstOrNull { it.tekst() == "Gammel!" }.shouldBeNull()

        telteAntall.permutasjoner.sumOf { it.antall } shouldBe 7
    }

    @Test
    fun `teller totalt antall varseltekster opprettet før dato`() {
        fillDb(7, "Ny!", tidspunkt = LocalDateTime.now())
        fillDb(5, "Gammel!", tidspunkt = LocalDateTime.now().minusDays(10))

        val fiveDaysAgo = LocalDate.now().minusDays(5)

        val telteAntall = tellAntallVarselteksterTotalt(Teksttype.WebTekst, sluttDato = fiveDaysAgo)


        telteAntall.permutasjoner.firstOrNull { it.tekst() == "Ny!" }.shouldBeNull()
        telteAntall.permutasjoner.first { it.tekst() == "Gammel!" }.antall shouldBe 5

        telteAntall.permutasjoner.sumOf { it.antall } shouldBe 5
    }

    @Test
    fun `teller totalt antall varseltekster av type`() {
        fillDb(3, "Beskjed!", varseltype = "beskjed")
        fillDb(5, "Oppgave!", varseltype = "oppgave")
        fillDb(7, "Innboks!", varseltype = "innboks")

        val telteAntall = tellAntallVarselteksterTotalt(Teksttype.WebTekst, varseltype = "innboks")

        telteAntall.permutasjoner.sumOf { it.antall } shouldBe 7
    }

    @Test
    fun `kan telle uten standardtekst for eksterne tekster`() {
        fillDb(3, smsSendt = true, smsTekst = "En sms med egendefinert tekst!")
        fillDb(7, smsSendt = true, smsTekst = null)

        val telteAntall = tellAntallVarselteksterTotalt(Teksttype.SmsTekst)

        telteAntall.permutasjoner.sumOf { it.antall } shouldBe 3
    }

    @Test
    fun `kan telle med standardtekst for eksterne tekster`() {
        fillDb(3, smsSendt = true, smsTekst = "En sms med egendefinert tekst!")
        fillDb(7, smsSendt = true, smsTekst = null)

        val telteAntall = tellAntallVarselteksterTotalt(Teksttype.SmsTekst, inkluderStandardtekster = true)

        telteAntall.permutasjoner.sumOf { it.antall } shouldBe 10
    }

    @Test
    fun `data kommer sortert i synkende antall`() {
        fillDb(3, "Tekst 1")
        fillDb(23, "Tekst 2")
        fillDb(17, "Tekst 3")
        fillDb(11, "Tekst 4")
        fillDb(29, "Tekst 5")

        val telteAntall = tellAntallVarselteksterTotalt(Teksttype.WebTekst)

        telteAntall.permutasjoner.shouldBeSortedDescendingBy { it.antall }
    }

    private fun tellAntallVarselteksterTotalt(
        teksttype: Teksttype,
        varseltype: String? = null,
        startDato: LocalDate? = null,
        sluttDato: LocalDate? = null,
        inkluderStandardtekster: Boolean = false
    ) = varseltekstRepository.tellAntallVarselteksterTotalt(
        listOf(teksttype),
        varseltype,
        startDato,
        sluttDato,
        inkluderStandardtekster
    )

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

    private fun TotaltAntall.Permutasjon.tekst(): String? {
        if (tekster.size != 1) throw IllegalArgumentException("Permutasjon kan kun ha 1 tekst ved bruk av tekst()")

        return tekster.first().tekst
    }
}
