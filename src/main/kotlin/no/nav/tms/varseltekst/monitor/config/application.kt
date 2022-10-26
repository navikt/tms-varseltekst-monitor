package no.nav.tms.varseltekst.monitor.config

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

fun main() {
    val appContext = ApplicationContext()

    startRapid(appContext)
}

private fun startRapid(appContext: ApplicationContext) {
    RapidApplication.create(appContext.environment.kafkaEnvironment).apply {
        register(object : RapidsConnection.StatusListener {
            override fun onStartup(rapidsConnection: RapidsConnection) {
                Flyway.runFlywayMigrations(appContext.environment)
                appContext.initCoalescingService()
                appContext.coalescingBacklogJob.start()
                registerSink(appContext.varselSink)
            }

            override fun onShutdown(rapidsConnection: RapidsConnection) {}
        })
    }.start()
}
