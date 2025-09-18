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

class AntallVarselRepositoryTest {
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

        telteAntall.permutasjoner.first { it.varseltype == "oppgave" }.antall shouldBe 10
        telteAntall.permutasjoner.first { it.varseltype == "innboks" }.antall shouldBe 8

        telteAntall.permutasjoner.sumOf { it.antall } shouldBe 18

        telteAntall.teksttyper shouldBe listOf(Teksttype.WebTekst)
    }

    @Test
    fun `teller antall varseltekster per produsent`() {
        fillDb(3, "Hallo!", produsent = Produsent("team-tim", "appen"))
        fillDb(5, "Hallo!", produsent = Produsent("team-tim", "appto"))
        fillDb(13, "Hallo!", produsent = Produsent("team-annet", "app-api"))

        val telteAntall = tellAntallVarseltekster(Teksttype.WebTekst)

        telteAntall.permutasjoner.first { it.produsent.appnavn == "appen" }.antall shouldBe 3
        telteAntall.permutasjoner.first { it.produsent.appnavn == "appto" }.antall shouldBe 5
        telteAntall.permutasjoner.first { it.produsent.appnavn == "app-api" }.antall shouldBe 13

        telteAntall.permutasjoner.sumOf { it.antall } shouldBe 21

        telteAntall.teksttyper shouldBe listOf(Teksttype.WebTekst)
    }

    @Test
    fun `teller antall varseltekster opprettet etter dato`() {
        fillDb(7, "Ny!", tidspunkt = LocalDateTime.now())
        fillDb(5, "Gammel!", tidspunkt = LocalDateTime.now().minusDays(10))

        val fiveDaysAgo = LocalDate.now().minusDays(5)

        val telteAntall = tellAntallVarseltekster(Teksttype.WebTekst, startDato = fiveDaysAgo)

        telteAntall.permutasjoner.first { it.tekst() == "Ny!" }.antall shouldBe 7
        telteAntall.permutasjoner.firstOrNull { it.tekst() == "Gammel!" }.shouldBeNull()

        telteAntall.permutasjoner.sumOf { it.antall } shouldBe 7

        telteAntall.teksttyper shouldBe listOf(Teksttype.WebTekst)
    }

    @Test
    fun `teller antall varseltekster opprettet før dato`() {
        fillDb(7, "Ny!", tidspunkt = LocalDateTime.now())
        fillDb(5, "Gammel!", tidspunkt = LocalDateTime.now().minusDays(10))

        val fiveDaysAgo = LocalDate.now().minusDays(5)

        val telteAntall = tellAntallVarseltekster(Teksttype.WebTekst, sluttDato = fiveDaysAgo)

        telteAntall.permutasjoner.firstOrNull { it.tekst() == "Ny!" }.shouldBeNull()
        telteAntall.permutasjoner.first { it.tekst() == "Gammel!" }.antall shouldBe 5

        telteAntall.permutasjoner.sumOf { it.antall } shouldBe 5

        telteAntall.teksttyper shouldBe listOf(Teksttype.WebTekst)
    }

    @Test
    fun `teller antall varseltekster av type`() {
        fillDb(3, "Beskjed!", varseltype = "beskjed")
        fillDb(5, "Oppgave!", varseltype = "oppgave")
        fillDb(7, "Innboks!", varseltype = "innboks")

        val telteAntall = tellAntallVarseltekster(Teksttype.WebTekst, varseltype = "innboks")

        telteAntall.permutasjoner.sumOf { it.antall } shouldBe 7

        telteAntall.teksttyper shouldBe listOf(Teksttype.WebTekst)
    }

    @Test
    fun `kan telle uten standardtekst for eksterne tekster`() {
        fillDb(3, smsSendt = true, smsTekst = "En sms med egendefinert tekst!")
        fillDb(7, smsSendt = true, smsTekst = null)

        val telteAntall = tellAntallVarseltekster(Teksttype.SmsTekst, inkluderStandardtekster = false)

        telteAntall.permutasjoner.sumOf { it.antall } shouldBe 3

        telteAntall.teksttyper shouldBe listOf(Teksttype.SmsTekst)
    }

    @Test
    fun `kan telle med standardtekst for eksterne tekster`() {
        fillDb(3, smsSendt = true, smsTekst = "En sms med egendefinert tekst!")
        fillDb(7, smsSendt = true, smsTekst = null)

        val telteAntall = tellAntallVarseltekster(Teksttype.SmsTekst, inkluderStandardtekster = true)

        telteAntall.permutasjoner.sumOf { it.antall } shouldBe 10

        telteAntall.teksttyper shouldBe listOf(Teksttype.SmsTekst)
    }

    @Test
    fun `data kommer sortert i synkende antall`() {
        fillDb(3, "Tekst 1", varseltype = "oppgave")
        fillDb(23, "Tekst 2", varseltype = "oppgave")
        fillDb(17, "Tekst 3", varseltype = "innboks")
        fillDb(11, "Tekst 4", varseltype = "beskjed")
        fillDb(29, "Tekst 5", varseltype = "oppgave")

        val telteAntall = tellAntallVarseltekster(Teksttype.WebTekst)
        telteAntall.permutasjoner.shouldBeSortedDescendingBy { it.antall }
        telteAntall.teksttyper shouldBe listOf(Teksttype.WebTekst)
    }

    @Test
    fun `teller antall varseltekster av flere typer samtidig`() {
        fillDb(10, "Hei!", smsTekst = "SMS!")
        fillDb(5, "Hallo!", smsTekst = "SMS!")
        fillDb(7, "Hallo!", smsTekst = "Annen SMS!")
        fillDb(2, "Hallo!", smsTekst = "Annen SMS!", epostTekst = "Epost..")

        val telteAntall = tellAntallVarseltekster(listOf(Teksttype.WebTekst, Teksttype.SmsTekst))

        telteAntall.permutasjoner.size shouldBe 3

        telteAntall.permutasjoner.first { it.tekst(0) == "Hei!" }.antall shouldBe 10
        telteAntall.permutasjoner.first { it.tekst(0) == "Hallo!" && it.tekst(1) == "SMS!" }.antall shouldBe 5
        telteAntall.permutasjoner.first { it.tekst(0) == "Hallo!" && it.tekst(1) == "Annen SMS!" }.antall shouldBe 9

        telteAntall.permutasjoner.sumOf { it.antall } shouldBe 24

        telteAntall.teksttyper shouldBe listOf(Teksttype.WebTekst, Teksttype.SmsTekst)
    }

    @Test
    fun `teller antall varseltekster av flere typer samtidig inkludert standardtekst`() {
        fillDb(10, "Hallo!", smsTekst = "SMS!")
        fillDb(5, "Hallo!", smsTekst = "SMS!", epostTekst = "Epost..")
        fillDb(7, "Hallo!", smsTekst = null, smsSendt = true)
        fillDb(2, "Hallo!", smsTekst = null, smsSendt = true)

        val telteAntall = tellAntallVarseltekster(listOf(Teksttype.WebTekst, Teksttype.SmsTekst), inkluderStandardtekster = true)

        telteAntall.permutasjoner.size shouldBe 2
        telteAntall.permutasjoner[0].let {
            it.tekster[0].tekst shouldBe "Hallo!"
            it.tekster[0].innhold shouldBe Tekst.Innhold.Egendefinert
            it.tekster[1].tekst shouldBe "SMS!"
            it.tekster[1].innhold shouldBe Tekst.Innhold.Egendefinert
            it.antall shouldBe 15
        }

        telteAntall.permutasjoner[1].let {
            it.tekster[0].tekst shouldBe "Hallo!"
            it.tekster[0].innhold shouldBe Tekst.Innhold.Egendefinert
            it.tekster[1].tekst shouldBe null
            it.tekster[1].innhold shouldBe Tekst.Innhold.Standard
            it.antall shouldBe 9
        }

        telteAntall.permutasjoner.sumOf { it.antall } shouldBe 24

        telteAntall.teksttyper shouldBe listOf(Teksttype.WebTekst, Teksttype.SmsTekst)
    }

    @Test
    fun `teller antall varseltekster av flere typer per varseltype`() {
        fillDb(10, "Hallo!", smsTekst = "Hei, SMS!", varseltype = "oppgave")
        fillDb(3, "Hallo!", smsTekst = "Hei, SMS!", varseltype = "innboks")
        fillDb(5, "Hallo!", smsTekst = "Hei, SMS!", varseltype = "innboks")

        val telteAntall = tellAntallVarseltekster(listOf(Teksttype.WebTekst, Teksttype.SmsTekst))

        telteAntall.permutasjoner.first { it.varseltype == "oppgave" }.antall shouldBe 10
        telteAntall.permutasjoner.first { it.varseltype == "innboks" }.antall shouldBe 8

        telteAntall.permutasjoner.sumOf { it.antall } shouldBe 18

        telteAntall.teksttyper shouldBe listOf(Teksttype.WebTekst, Teksttype.SmsTekst)
    }

    @Test
    fun `teller antall varseltekster av flere typer per produsent`() {
        fillDb(3, "Hallo!", smsTekst = "Hei, SMS!", produsent = Produsent("team-tim", "appen"))
        fillDb(5, "Hallo!", smsTekst = "Hei, SMS!", produsent = Produsent("team-tim", "appto"))
        fillDb(13, "Hallo!", smsTekst = "Hei, SMS!", produsent = Produsent("team-annet", "app-api"))

        val telteAntall = tellAntallVarseltekster(listOf(Teksttype.WebTekst, Teksttype.SmsTekst))

        telteAntall.permutasjoner.first { it.produsent.appnavn == "appen" }.antall shouldBe 3
        telteAntall.permutasjoner.first { it.produsent.appnavn == "appto" }.antall shouldBe 5
        telteAntall.permutasjoner.first { it.produsent.appnavn == "app-api" }.antall shouldBe 13

        telteAntall.permutasjoner.sumOf { it.antall } shouldBe 21

        telteAntall.teksttyper shouldBe listOf(Teksttype.WebTekst, Teksttype.SmsTekst)
    }

    @Test
    fun `beholder rekkefølgen på kolonner basert på parameter`() {
        fillDb(10, "WEB!", smsTekst = "SMS!")

        tellAntallVarseltekster(listOf(Teksttype.WebTekst, Teksttype.SmsTekst)).let {
            it.permutasjoner.first().tekst(0) shouldBe "WEB!"
            it.permutasjoner.first().tekst(1) shouldBe "SMS!"
            it.teksttyper shouldBe listOf(Teksttype.WebTekst, Teksttype.SmsTekst)
        }

        tellAntallVarseltekster(listOf(Teksttype.SmsTekst, Teksttype.WebTekst)).let {
            it.permutasjoner.first().tekst(0) shouldBe "SMS!"
            it.permutasjoner.first().tekst(1) shouldBe "WEB!"
            it.teksttyper shouldBe listOf(Teksttype.SmsTekst, Teksttype.WebTekst)
        }
    }

    @Test
    fun `kan telle kun varsler med ønsket tekst`() {
        fillDb(10, "WEB!", smsTekst = "SMS!")
        fillDb(7, "WEB!")
        fillDb(5, "WEB!", epostTekst = "EPOST")

        tellAntallVarseltekster(listOf(Teksttype.WebTekst), inkluderUbrukt = false).let {
            it.permutasjoner.sumOf { it.antall } shouldBe 22
        }

        tellAntallVarseltekster(listOf(Teksttype.SmsTekst), inkluderUbrukt = false).let {
            it.permutasjoner.sumOf { it.antall } shouldBe 10
        }

        tellAntallVarseltekster(listOf(Teksttype.WebTekst, Teksttype.SmsTekst), inkluderUbrukt = false).let {
            it.permutasjoner.sumOf { it.antall } shouldBe 10
        }

        tellAntallVarseltekster(listOf(Teksttype.WebTekst, Teksttype.SmsTekst, Teksttype.EpostTekst), inkluderUbrukt = false).let {
            it.permutasjoner.sumOf { it.antall } shouldBe 0
        }
    }

    @Test
    fun `kan telle alle varsler selv uten ønsket tekst`() {
        fillDb(10, "WEB!", smsTekst = "SMS!")
        fillDb(7, "WEB!")
        fillDb(5, "WEB!", epostTekst = "EPOST")

        tellAntallVarseltekster(listOf(Teksttype.WebTekst), inkluderUbrukt = true).let {
            it.permutasjoner.sumOf { it.antall } shouldBe 22
        }

        tellAntallVarseltekster(listOf(Teksttype.SmsTekst), inkluderUbrukt = true).let {
            it.permutasjoner.sumOf { it.antall } shouldBe 22
            it.permutasjoner.size shouldBe 2
            it.permutasjoner[0].let { manglende ->
                manglende.tekster.first().tekst shouldBe null
                manglende.tekster.first().innhold shouldBe Tekst.Innhold.Ubrukt
                manglende.antall shouldBe 12
            }
            it.permutasjoner[1].let { harSms ->
                harSms.antall shouldBe 10
                harSms.tekster.first().tekst shouldBe "SMS!"
                harSms.tekster.first().innhold shouldBe Tekst.Innhold.Egendefinert
            }
        }

        tellAntallVarseltekster(listOf(Teksttype.WebTekst, Teksttype.SmsTekst), inkluderUbrukt = true).let {
            it.permutasjoner.sumOf { it.antall } shouldBe 22
            it.permutasjoner[0].let { kunWeb ->
                kunWeb.tekster[0].tekst shouldBe "WEB!"
                kunWeb.tekster[0].innhold shouldBe Tekst.Innhold.Egendefinert
                kunWeb.tekster[1].tekst shouldBe null
                kunWeb.tekster[1].innhold shouldBe Tekst.Innhold.Ubrukt
                kunWeb.antall shouldBe 12
            }
            it.permutasjoner[1].let { harSms ->
                harSms.tekster[0].tekst shouldBe "WEB!"
                harSms.tekster[0].innhold shouldBe Tekst.Innhold.Egendefinert
                harSms.tekster[1].tekst shouldBe "SMS!"
                harSms.tekster[1].innhold shouldBe Tekst.Innhold.Egendefinert
            }
        }

        tellAntallVarseltekster(listOf(Teksttype.WebTekst, Teksttype.SmsTekst, Teksttype.EpostTekst), inkluderUbrukt = true).let {
            it.permutasjoner.sumOf { it.antall } shouldBe 22
            it.permutasjoner[0].let { harSms ->
                harSms.tekster[0].tekst shouldBe "WEB!"
                harSms.tekster[0].innhold shouldBe Tekst.Innhold.Egendefinert
                harSms.tekster[1].tekst shouldBe "SMS!"
                harSms.tekster[1].innhold shouldBe Tekst.Innhold.Egendefinert
                harSms.tekster[2].tekst shouldBe null
                harSms.tekster[2].innhold shouldBe Tekst.Innhold.Ubrukt
                harSms.antall shouldBe 10
            }
            it.permutasjoner[1].let { kunWeb ->
                kunWeb.tekster[0].tekst shouldBe "WEB!"
                kunWeb.tekster[0].innhold shouldBe Tekst.Innhold.Egendefinert
                kunWeb.tekster[1].tekst shouldBe null
                kunWeb.tekster[1].innhold shouldBe Tekst.Innhold.Ubrukt
                kunWeb.tekster[2].tekst shouldBe null
                kunWeb.tekster[2].innhold shouldBe Tekst.Innhold.Ubrukt
                kunWeb.antall shouldBe 7
            }
            it.permutasjoner[2].let { harEpost ->
                harEpost.tekster[0].tekst shouldBe "WEB!"
                harEpost.tekster[0].innhold shouldBe Tekst.Innhold.Egendefinert
                harEpost.tekster[1].tekst shouldBe null
                harEpost.tekster[1].innhold shouldBe Tekst.Innhold.Ubrukt
                harEpost.tekster[2].tekst shouldBe "EPOST"
                harEpost.tekster[2].innhold shouldBe Tekst.Innhold.Egendefinert
                harEpost.antall shouldBe 5
            }
        }
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
        inkluderStandardtekster: Boolean = false,
        inkluderUbrukt: Boolean = false
    ) = varseltekstRepository.tellAntallVarseltekster(
        listOf(teksttype),
        varseltype,
        startDato,
        sluttDato,
        inkluderStandardtekster,
        inkluderUbrukt
    )

    fun tellAntallVarseltekster(
        teksttyper: List<Teksttype>,
        varseltype: String? = null,
        startDato: LocalDate? = null,
        sluttDato: LocalDate? = null,
        inkluderStandardtekster: Boolean = false,
        inkluderUbrukt: Boolean = false
    ) = varseltekstRepository.tellAntallVarseltekster(
        teksttyper,
        varseltype,
        startDato,
        sluttDato,
        inkluderStandardtekster,
        inkluderUbrukt
    )

    private fun DetaljertAntall.Permutasjon.tekst(): String? {
        if (tekster.size != 1) throw IllegalArgumentException("Permutasjon kan kun ha 1 tekst ved bruk av tekst()")

        return tekster.first().tekst
    }

    private fun DetaljertAntall.Permutasjon.tekst(index: Int): String? {
        return tekster[index].tekst
    }
}
