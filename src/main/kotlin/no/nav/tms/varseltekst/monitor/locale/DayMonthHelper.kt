package no.nav.tms.varseltekst.monitor.locale

import java.util.*

object DayMonthHelper {
    private val calendar = GregorianCalendar()

    fun daysOfWeek(locale: Locale): List<String> {
        return calendar.getDisplayNames(Calendar.DAY_OF_WEEK, Calendar.LONG_FORMAT, locale).keys.toList()
    }

    fun monthsOfYear(locale: Locale): List<String> {
        return calendar.getDisplayNames(Calendar.MONTH, Calendar.LONG_FORMAT, locale).keys.toList()
    }
}
