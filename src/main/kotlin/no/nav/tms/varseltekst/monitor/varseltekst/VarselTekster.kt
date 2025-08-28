package no.nav.tms.varseltekst.monitor.varseltekst

object VarselTekster {
    data class TotaltAntall(
        val antall: Int,
        val tekst: String
    )

    data class DetaljertAntall(
        val varseltype: String,
        val produsentNamespace: String,
        val produsentAppnavn: String,
        val antall: Int,
        val tekst: String
    )
}
