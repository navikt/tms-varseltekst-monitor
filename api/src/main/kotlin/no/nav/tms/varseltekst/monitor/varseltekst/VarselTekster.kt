package no.nav.tms.varseltekst.monitor.varseltekst

object VarselTekster {
    data class TotaltAntall(
        val antall: Int,
        val tekst: String?
    )

    data class DetaljertAntall(
        val varseltype: String,
        val produsent: Produsent,
        val antall: Int,
        val tekst: String?
    )

    data class Produsent(
        val namespace: String,
        val appnavn: String
    )
}
