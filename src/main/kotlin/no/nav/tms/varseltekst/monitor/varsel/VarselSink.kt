package no.nav.tms.varseltekst.monitor.varsel

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.River
import no.nav.tms.varseltekst.monitor.config.PacketValidator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

class VarselSink(
    private val varselRepository: VarselRepository,
) : River.PacketListener, PacketValidator {

    private val log: Logger = LoggerFactory.getLogger(VarselSink::class.java)

    override fun packetValidator(): River.() -> Unit = {
        validate { it.demandValue("@event_name", "aktivert") }
        validate { it.requireKey(
                "eventId",
                "varselType",
                "namespace",
                "appnavn",
                "forstBehandlet",
                "tekst",
                "eksternVarsling",
                "prefererteKanaler"
            )
        }
        validate { it.interestedIn( "smsVarslingstekst", "epostVarslingstittel", "epostVarslingstekst",)}
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val varsel = Varsel(
            eventId = packet["eventId"].textValue(),
            eventType = packet["varselType"].textValue(),
            producerNamespace = packet["namespace"].textValue(),
            producerAppnavn = packet["appnavn"].textValue(),
            eksternVarsling = packet["eksternVarsling"].booleanValue(),
            preferertKanalSms = isPrefererertKanalSms(packet),
            preferertKanalEpost = isPreferertKanalEpost(packet),
            webTekst = packet["tekst"].textValue(),
            smsTekst = packet["smsVarslingstekst"].textValue(),
            epostTittel = packet["epostVarslingstittel"].textValue(),
            epostTekst = packet["epostVarslingstekst"].textValue(),
            varseltidspunkt = parseTidspunkt(packet)
        )

        varselRepository.persistVarsel(varsel)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error(problems.toString())
    }

    private fun isPrefererertKanalSms(jsonMessage: JsonMessage): Boolean {
        return jsonMessage["prefererteKanaler"].map { it.textValue() }.contains("SMS")
    }

    private fun isPreferertKanalEpost(jsonMessage: JsonMessage): Boolean {
        return jsonMessage["prefererteKanaler"].map{ it.textValue() }.contains("EPOST")
    }

    private fun parseTidspunkt(jsonMessage: JsonMessage): LocalDateTime {
        return LocalDateTime.parse(jsonMessage["forstBehandlet"].asText())
    }
}
