package no.nav.tms.varseltekst.monitor.health

import no.nav.tms.varseltekst.monitor.config.ApplicationContext

class HealthService(private val applicationContext: ApplicationContext) {

    fun getHealthChecks(): List<HealthStatus> {
        return listOf(
            applicationContext.database.status(),
        )
    }
}
