package no.nav.tms.varseltekst.monitor.varsel

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.River
import no.nav.tms.varseltekst.monitor.coalesce.CoalescingService
import no.nav.tms.varseltekst.monitor.config.PacketValidator
import no.nav.tms.varseltekst.monitor.util.defaultDeserializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime

class VarselSink(
    private val coalescingService: CoalescingService,
    private val varselRepository: VarselRepository,
) : River.PacketListener, PacketValidator {

    private val log = KotlinLogging.logger {}
    private val secureLog = KotlinLogging.logger("secureLog")

    private val objectMapper = defaultDeserializer()

    override fun packetValidator(): River.() -> Unit = {
        validate { it.demandValue("@event_name", "aktivert") }
        validate { it.requireKey(
                "varselId",
                "type",
                "produsent",
                "innhold",
                "opprettet"
            )
        }
        validate { it.interestedIn( "eksternVarslingBestilling")}
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val aktivertVarsel: AktivertVarsel = objectMapper.readValue(packet.toJson())

        varselRepository.persistVarsel(coalesceAndBuildOversikt(aktivertVarsel))
    }

    private fun coalesceAndBuildOversikt(varsel: AktivertVarsel) = VarselOversikt(
        eventId = varsel.varselId,
        eventType = varsel.type,
        producerNamespace = varsel.produsent.namespace,
        producerAppnavn = varsel.produsent.appnavn,
        eksternVarsling = varsel.eksternVarslingBestilling != null,
        preferertKanalSms = varsel.eksternVarslingBestilling?.prefererteKanaler?.containsIgnoreCase("sms") ?: false,
        preferertKanalEpost = varsel.eksternVarslingBestilling?.prefererteKanaler?.containsIgnoreCase("epost") ?: false,
        webTekst = varsel.innhold.tekst.coalesced(),
        smsTekst = varsel.eksternVarslingBestilling?.smsVarslingstekst?.coalesced(),
        epostTittel = varsel.eksternVarslingBestilling?.epostVarslingstittel?.coalesced(),
        epostTekst = varsel.eksternVarslingBestilling?.epostVarslingstekst?.coalesced(),
        varseltidspunkt = varsel.opprettet.toUtcLocalDateTime(),
    )


    private fun ZonedDateTime.toUtcLocalDateTime() = withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()
    private fun List<String>.containsIgnoreCase(string: String) = map { it.lowercase() }.contains(string.lowercase())

    private fun String.coalesced() = coalescingService.coalesce(this)
        .also {
            if (it.isCoalesced) {
                val rules = it.rulesApplied.map { rule -> rule.name }.joinToString(", ")
                log.info { "Lagrer varsel med modifisert tekst etter regler [$rules]" }
                secureLog.info { "Lagrer varsel modifisert tekst { original: \"${it.originalTekst}\", final: \"${it.finalTekst}\" } etter regler [$rules]" }
            }
        }
        .finalTekst

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error { "$problems" }
    }
}
