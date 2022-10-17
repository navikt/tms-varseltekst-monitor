package no.nav.tms.varseltekst.monitor.common

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

object LocalDateTimeHelper {
    val EPOCH_START = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)

    fun timestampToUTCDateOrNull(timestamp: Long?): LocalDateTime? {
        return timestamp?.let { datetime ->
            LocalDateTime.ofInstant(Instant.ofEpochMilli(datetime), ZoneId.of("UTC"))
        }
    }

    // Fix instances where date was serialized as epochSeconds but interpreted as epochMillis
    private val lowerEpochThreshold = Instant.parse("1971-01-01T00:00:00.00Z").toEpochMilli()

    fun epochToLocalDateTimeFixIfTruncated(epochMillis: Long): LocalDateTime {
        return if (epochMillis < lowerEpochThreshold) {
            val adjustedEpoch = epochMillis * 1000
            epochMillisToLocalDateTime(adjustedEpoch)
        } else {
            epochMillisToLocalDateTime(epochMillis)
        }
    }

    fun epochMillisToLocalDateTime(epochMillis: Long): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.of("UTC"))
    }

    fun nowAtUtc(): LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
}

