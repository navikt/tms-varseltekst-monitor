package no.nav.tms.varseltekst.monitor.varseltekst

import io.kotest.matchers.shouldBe
import no.nav.tms.varseltekst.monitor.setup.LocalPostgresDatabase
import no.nav.tms.varseltekst.monitor.setup.clearAllTables
import no.nav.tms.varseltekst.monitor.varsel.Produsent
import no.nav.tms.varseltekst.monitor.varsel.VarselOversikt
import no.nav.tms.varseltekst.monitor.varsel.VarselRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class VarseltekstRoutesTest {
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

        val sammendrag = varseltekstRepository.finnForenkletSammendrag(
            Teksttype.WebTekst,
            varseltype = null,
            maksAlderDager = null
        )

        sammendrag.first { it.tekst == "Hei!" }.antall shouldBe 10
        sammendrag.first { it.tekst == "Hallo!" }.antall shouldBe 5

        sammendrag.sumOf { it.antall } shouldBe 15
    }

    @Test
    fun `teller totalt antall varseltekster opprettet etter dato`() {
        fillDb(7, "Ny!", tidspunkt = LocalDateTime.now())
        fillDb(5, "Gammel!", tidspunkt = LocalDateTime.now().minusDays(10))

        val sammendrag = varseltekstRepository.finnForenkletSammendrag(
            Teksttype.WebTekst,
            varseltype = null,
            maksAlderDager = 5
        )

        sammendrag.first { it.tekst == "Ny!" }.antall shouldBe 7
        sammendrag.find { it.tekst == "Gammel!" }?.antall shouldBe null

        sammendrag.sumOf { it.antall } shouldBe 7
    }

    @Test
    fun `teller totalt antall varseltekster av type`() {
        fillDb(3, "Beskjed!", varseltype = "beskjed")
        fillDb(5, "Oppgave!", varseltype = "oppgave")
        fillDb(7, "Innboks!", varseltype = "innboks")

        val sammendrag = varseltekstRepository.finnForenkletSammendrag(
            Teksttype.WebTekst,
            varseltype = "innboks",
            maksAlderDager = null
        ).first()

        sammendrag.antall shouldBe 7
        sammendrag.tekst shouldBe "Innboks!"
    }

    private fun fillDb(
        antall: Int,
        webTekst: String = "Hei hallo på min side",
        smsTekst: String? = null,
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
                eksternVarsling = smsTekst != null || epostTekst != null,
                preferertKanalSms = smsTekst != null,
                preferertKanalEpost = epostTekst != null,
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
}
