package no.nav.tms.varseltekst.monitor.database

open class RetriableDatabaseException(message: String, cause: Throwable?) : Exception(message, cause) {
    constructor(message: String) : this(message, null)
}
