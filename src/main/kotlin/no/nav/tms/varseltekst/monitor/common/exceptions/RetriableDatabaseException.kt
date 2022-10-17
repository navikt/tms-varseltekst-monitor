package no.nav.tms.varseltekst.monitor.common.exceptions

open class RetriableDatabaseException(message: String, cause: Throwable?) : Exception(message, cause) {
    constructor(message: String) : this(message, null)
}
