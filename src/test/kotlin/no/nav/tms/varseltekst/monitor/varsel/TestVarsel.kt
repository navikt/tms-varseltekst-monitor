package no.nav.tms.varseltekst.monitor.varsel

import java.time.LocalDateTime
import java.util.*

object TestVarsel {

    private val defaultEventId get() = UUID.randomUUID().toString()
    private val defaultEventType = "beskjed"
    private val defaultProducerNamespace = "namespace"
    private val defaultProducerApp = "app"
    private val defaultEksternVarsling = false
    private val defaultPreferertKanalSms = false
    private val defaultPreferertKanalEpost = false
    private val defaultWebTekst = "Web tekst"
    private val defaultSmsTekst: String? = null
    private val defaultEpostTittel: String? = null
    private val defaultEpostTekst: String? = null
    private val defaultVarseltidspunkt get() = LocalDateTime.now()

    fun createVarsel(
        eventId: String = defaultEventId,
        eventType: String = defaultEventType,
        producerNamespace: String = defaultProducerNamespace,
        producerApp: String = defaultProducerApp,
        eksternVarsling: Boolean = defaultEksternVarsling,
        preferertKanalSms: Boolean = defaultPreferertKanalSms,
        preferertKanalEpost: Boolean = defaultPreferertKanalEpost,
        webTekst: String = defaultWebTekst,
        smsTekst: String? = defaultSmsTekst,
        epostTittel: String? = defaultEpostTittel,
        epostTekst: String? = defaultEpostTekst,
        varseltidspunkt: LocalDateTime = defaultVarseltidspunkt,
    ) = VarselOversikt(
        eventId = eventId,
        eventType = eventType,
        producerNamespace = producerNamespace,
        producerAppnavn = producerApp,
        eksternVarsling = eksternVarsling,
        preferertKanalSms = preferertKanalSms,
        preferertKanalEpost = preferertKanalEpost,
        webTekst = webTekst,
        smsTekst = smsTekst,
        epostTittel = epostTittel,
        epostTekst = epostTekst,
        varseltidspunkt = varseltidspunkt
    )
}
