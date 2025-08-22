package no.nav.tms.varseltekst.monitor.varseltekst

object VarselTekster {
    data class TotaltAntall(
        val antall: Int,
        val tekst: String
    )

    data class DetaljertAntall(
        val type: String,
        val produsent: String,
        val antall: Int,
        val tekst: String
    )
}
