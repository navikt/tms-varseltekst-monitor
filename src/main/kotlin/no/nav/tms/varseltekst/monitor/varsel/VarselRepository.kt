package no.nav.tms.varseltekst.monitor.varsel

import no.nav.tms.varseltekst.monitor.common.LocalDateTimeHelper
import no.nav.tms.varseltekst.monitor.common.database.Database

class VarselRepository(private val database: Database) {

    fun persistVarsel(varsel2: Varsel) {
        val varselDto = VarselDto (
            eventId = varsel2.eventId,
            eventType = varsel2.eventType,
            producerNamespace = varsel2.producerNamespace,
            producerAppnavn = varsel2.producerAppnavn,
            preferertKanalSms = varsel2.preferertKanalSms,
            preferertKanalEpost = varsel2.preferertKanalEpost,
            webTekstRef = persistWebTekst(varsel2.webTekst),
            smsTekstRef = varsel2.smsTekst?.let { persistSmsTekst(it) },
            epostTittelRef = varsel2.epostTittel?.let { persistEpostTittel(it) },
            epostTekstRef = varsel2.epostTekst?.let { persistEpostTekst(it) },
            varseltidspunkt = varsel2.varseltidspunkt,
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
