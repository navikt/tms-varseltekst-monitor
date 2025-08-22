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

    fun finnForenkletSammendrag(teksttype: TekstType, maksAlderDager: Long?, varseltype: String?): List<VarselTekster.TotaltAntall> {
        return database.list {
            queryOf("""
                select
                    count(*) as antall,
                    vt.tekst
                from
                    varsel join ${teksttype.columnTableName} tt on ${teksttype.columnTableName} = tt.id
                where
                    (:dager is null or varsel.tidspunkt > :dato) and
                    (:varseltype is null or varsel.event_type = :varseltype)
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

enum class TekstType(val columnTableName: String) {
    WebTekst("web_tekst"),
    SmsTekst("sms_tekst"),
    EpostTittel("epost_tittel"),
    EpostTekst("epost_tekst")
}
