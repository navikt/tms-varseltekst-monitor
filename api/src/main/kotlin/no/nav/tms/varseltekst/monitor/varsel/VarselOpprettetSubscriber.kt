package no.nav.tms.varseltekst.monitor.varsel

import com.fasterxml.jackson.module.kotlin.treeToValue
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.tms.common.logging.TeamLogs
import no.nav.tms.kafka.application.JsonMessage
import no.nav.tms.kafka.application.Subscriber
import no.nav.tms.kafka.application.Subscription
import no.nav.tms.varseltekst.monitor.coalesce.CoalescingService
import no.nav.tms.varseltekst.monitor.util.defaultDeserializer
import java.time.ZoneOffset
import java.time.ZonedDateTime

class VarselOpprettetSubscriber(
    private val coalescingService: CoalescingService,
    private val varselRepository: VarselRepository,
) : Subscriber() {

    private val log = KotlinLogging.logger {}
    private val teamLog = TeamLogs.logger { }

    private val objectMapper = defaultDeserializer()

    override fun subscribe() = Subscription.forEvent("opprettet")
        .withFields(
            "varselId",
            "type",
            "produsent",
            "innhold",
            "opprettet"
        )
        .withOptionalFields("eksternVarslingBestilling")

    override suspend fun receive(jsonMessage: JsonMessage) {
        val aktivertVarsel: AktivertVarsel = objectMapper.treeToValue(jsonMessage.json)

        varselRepository.persistVarsel(coalesceAndBuildOversikt(aktivertVarsel))
    }

    private fun coalesceAndBuildOversikt(varsel: AktivertVarsel) = VarselOversikt(
        eventId = varsel.varselId,
        eventType = varsel.type,
        producerNamespace = varsel.produsent.namespace,
        producerAppnavn = varsel.produsent.appnavn,
        eksternVarsling = varsel.eksternVarslingBestilling != null,
        preferertKanalSms = varsel.eksternVarslingBestilling?.prefererteKanaler?.containsIgnoreCase("sms", "betinget_sms") ?: false,
        preferertKanalEpost = varsel.eksternVarslingBestilling?.prefererteKanaler?.containsIgnoreCase("epost") ?: false,
        webTekst = varsel.innhold.defaultTekst().coalesced(),
        smsTekst = varsel.eksternVarslingBestilling?.smsVarslingstekst?.coalesced(),
        epostTittel = varsel.eksternVarslingBestilling?.epostVarslingstittel?.coalesced(),
        epostTekst = varsel.eksternVarslingBestilling?.epostVarslingstekst?.coalesced(),
        varseltidspunkt = varsel.opprettet.toUtcLocalDateTime(),
    )


    private fun ZonedDateTime.toUtcLocalDateTime() = withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()
    private fun List<String>.containsIgnoreCase(vararg strings: String) = map { it.lowercase() }.any { strings.map { it.lowercase() }.contains(it) }

    private fun String.coalesced() = coalescingService.coalesce(this)
        .also {
            if (it.isCoalesced) {
                val rules = it.rulesApplied.map { rule -> rule.name }.joinToString(", ")
                log.info { "Lagrer varsel med modifisert tekst etter regler [$rules]" }
                teamLog.info { "Lagrer varsel modifisert tekst { original: \"${it.originalTekst}\", final: \"${it.finalTekst}\" } etter regler [$rules]" }
            }
        }
        .finalTekst
}
