package no.nav.tms.varseltekst.monitor.config

import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

fun <T> RapidsConnection.registerSink(sink: T)
    where T : PacketValidator, T: River.PacketListener {
    River(this)
        .also { river -> sink.packetValidator().invoke(river) }
        .register(sink)
}

interface PacketValidator {
    fun packetValidator(): River.() -> Unit
}
