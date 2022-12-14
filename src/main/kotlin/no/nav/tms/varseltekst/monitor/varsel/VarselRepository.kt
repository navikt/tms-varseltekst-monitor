package no.nav.tms.varseltekst.monitor.varsel

import no.nav.tms.varseltekst.monitor.common.LocalDateTimeHelper
import no.nav.tms.varseltekst.monitor.common.database.Database

class VarselRepository(private val database: Database) {

    fun persistVarsel(varsel: Varsel) {
        val varselDto = VarselDto (
            eventId = varsel.eventId,
            eventType = varsel.eventType,
            producerNamespace = varsel.producerNamespace,
            producerAppnavn = varsel.producerAppnavn,
            eksternVarsling = varsel.eksternVarsling,
            preferertKanalSms = varsel.preferertKanalSms,
            preferertKanalEpost = varsel.preferertKanalEpost,
            webTekstRef = persistWebTekst(varsel.webTekst),
            smsTekstRef = varsel.smsTekst?.let { persistSmsTekst(it) },
            epostTittelRef = varsel.epostTittel?.let { persistEpostTittel(it) },
            epostTekstRef = varsel.epostTekst?.let { persistEpostTekst(it) },
            varseltidspunkt = varsel.varseltidspunkt,
            tidspunkt = LocalDateTimeHelper.nowAtUtc()
        )

        database.queryWithExceptionTranslation {
            insertVarsel(varselDto)
        }
    }

    private fun persistWebTekst(tekst: String): Int = database.queryWithExceptionTranslation {
        upsertWebTekst(tekst)
    }


    private fun persistSmsTekst(tekst: String): Int = database.queryWithExceptionTranslation {
        upsertSmsTekst(tekst)
    }


    private fun persistEpostTittel(tekst: String): Int = database.queryWithExceptionTranslation {
        upsertEpostTittel(tekst)
    }


    private fun persistEpostTekst(tekst: String): Int = database.queryWithExceptionTranslation {
        upsertEpostTekst(tekst)
    }
}
