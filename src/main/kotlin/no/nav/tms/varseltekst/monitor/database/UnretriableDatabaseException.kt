package no.nav.tms.varseltekst.monitor.database

class UnretriableDatabaseException(message: String, cause: Throwable?) : Exception(message, cause) {
    constructor(message: String) : this(message, null)
}
