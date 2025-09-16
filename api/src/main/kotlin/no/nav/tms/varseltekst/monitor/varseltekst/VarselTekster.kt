package no.nav.tms.varseltekst.monitor.varseltekst

class DetaljertAntall(
    val teksttyper: List<Teksttype>,
    val permutasjoner: List<Permutasjon>
) {
    data class Permutasjon(
        val varseltype: String,
        val produsent: Produsent,
        val antall: Int,
        val tekster: List<Tekst>
    )

    data class Produsent(
        val namespace: String,
        val appnavn: String
    )
}

class TotaltAntall(
    val teksttyper: List<Teksttype>,
    val permutasjoner: List<Permutasjon>
) {
    data class Permutasjon(
        val antall: Int,
        val tekster: List<Tekst>
    )
}

data class Tekst(
    val tekst: String?,
) {
    val isStandard = tekst == null
}
