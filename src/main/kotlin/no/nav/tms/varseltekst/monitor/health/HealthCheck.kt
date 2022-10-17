package no.nav.tms.varseltekst.monitor.health

interface HealthCheck {

    fun status(): HealthStatus

}
