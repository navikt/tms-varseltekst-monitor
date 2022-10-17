package no.nav.tms.varseltekst.monitor.varsel

import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.River
import no.nav.tms.varseltekst.monitor.config.PacketValidator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDateTime

class VarselSink(
    private val varselRepository: VarselRepository,
) : River.PacketListener, PacketValidator {

    private val log: Logger = LoggerFactory.getLogger(VarselSink::class.java)

    override fun packetValidator(): River.() -> Unit = {
        validate { it.demandValue("eksternVarsling", true) }
        validate { it.requireKey(
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
        val varselTekster = Varseltekster(
            producer = parseProducer(packet),
            preferertKanalSms = isPrefererertKanalSms(packet),
            preferertKanalEpost = isPreferertKanalEpost(packet),
            tekst = packet["tekst"].textValue(),
            smsTekst = packet["smsVarslingstekst"].textValue(),
            epostTittel = packet["epostVarslingstittel"].textValue(),
            epostTekst = packet["epostVarslingstekst"].textValue(),
            tidspunkt = parseTidspunkt(packet)
        )

        runBlocking {
            varselRepository.persistVarselTekster(varselTekster)
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error(problems.toString())
    }

    private fun parseProducer(jsonMessage: JsonMessage): Producer {
        val namespace = jsonMessage["namespace"].textValue()
        val appnavn = jsonMessage["appnavn"].textValue()

        return Producer(namespace, appnavn)
    }

    private fun isPrefererertKanalSms(jsonMessage: JsonMessage): Boolean {
        return jsonMessage["prefererteKanaler"].textValue().uppercase().contains("(^|,)SMS(,|$)")
    }

    private fun isPreferertKanalEpost(jsonMessage: JsonMessage): Boolean {
        return jsonMessage["prefererteKanaler"].textValue().uppercase().contains("(^|,)EPOST(,|$)")
    }

    private fun parseTidspunkt(jsonMessage: JsonMessage): LocalDateTime {
        val epoch = jsonMessage["forstBehandlet"].longValue()

        return LocalDateTime.from(Instant.ofEpochMilli(epoch))
    }
}
