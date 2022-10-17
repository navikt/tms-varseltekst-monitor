package no.nav.tms.varseltekst.monitor.config

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

fun main() {
    val appContext = ApplicationContext()

    startRapid(appContext)
}

private fun startRapid(appContext: ApplicationContext) {
    RapidApplication.create(System.getenv()).apply {
        registerSink(appContext.varselSink)
    }.apply {
        register(object : RapidsConnection.StatusListener {
            override fun onStartup(rapidsConnection: RapidsConnection) {
                Flyway.runFlywayMigrations(appContext.environment)
            }

            override fun onShutdown(rapidsConnection: RapidsConnection) {}
        })
    }.start()
}
