package no.nav.tms.varseltekst.monitor.util

import java.time.LocalDateTime
import java.time.ZoneOffset

object LocalDateTimeHelper {
    fun nowAtUtc(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)
}

