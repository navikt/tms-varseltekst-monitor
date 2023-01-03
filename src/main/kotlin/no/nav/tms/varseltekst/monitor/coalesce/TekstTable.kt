package no.nav.tms.varseltekst.monitor.coalesce

enum class TekstTable {
    WEB_TEKST, SMS_TEKST, EPOST_TITTEL, EPOST_TEKST;

    val lowerCase = name.lowercase()
}
