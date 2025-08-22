package no.nav.tms.varseltekst.monitor.varseltekst

import kotliquery.queryOf
import no.nav.tms.varseltekst.monitor.setup.Database
import java.time.LocalDate

class VarseltekstRepository(private val database: Database) {

    private val totalDefaultBaseQuery = """
        select 
            count(*) as antall
        from
            varsel
        where 
            :preferert_column and
            :id_column is null
    """

    fun finnForenkletSammendrag(teksttype: Teksttype, maksAlderDager: Long?, varseltype: String?): List<VarselTekster.TotaltAntall> {
        return database.list {
            queryOf("""
                select
                    count(*) as antall,
                    tt.tekst
                from
                    varsel join ${teksttype.columnTableName} tt on ${teksttype.columnTableName} = tt.id
                where
                    ((:dato)::timestamp is null or varsel.varseltidspunkt > :dato) and
                    ((:varseltype)::text is null or varsel.event_type = :varseltype)
                group by tt.tekst
                order by antall desc
            """,
                mapOf(
                    "dato" to maksAlderDager?.let { LocalDate.now().minusDays(it) },
                    "varseltype" to varseltype
                ))
                .map {
                    VarselTekster.TotaltAntall(
                        antall = it.int("antall"),
                        tekst = it.string("tekst")
                    )
                }.asList
        }
    }
}

enum class Teksttype(val columnTableName: String) {
    WebTekst("web_tekst"),
    SmsTekst("sms_tekst"),
    EpostTittel("epost_tittel"),
    EpostTekst("epost_tekst");

    companion object {
        fun parse(string: String): Teksttype {
            return entries
                .firstOrNull { it.name.lowercase() == string.lowercase() }
                ?: throw IllegalArgumentException("Fant ikke teksttype for verdi $string")
        }
    }
}
