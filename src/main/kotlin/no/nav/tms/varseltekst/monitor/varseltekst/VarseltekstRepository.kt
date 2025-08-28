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

    fun tellAntallVarselteksterTotalt(
        teksttype: Teksttype,
        maksAlderDager: Long?,
        varseltype: String?,
        inkluderStandardtekster: Boolean
    ): List<VarselTekster.TotaltAntall> {

        val egendefinerteTekster = tellEgendefinerteTeksterTotalt(teksttype, maksAlderDager, varseltype)

        return if (inkluderStandardtekster && teksttype != Teksttype.WebTekst) {
            egendefinerteTekster + tellStandandardteksterTotalt(teksttype, maksAlderDager, varseltype)
        } else {
            egendefinerteTekster
        }.sortedByDescending {
            it.antall
        }
    }

    fun tellAntallVarseltekster(
        teksttype: Teksttype,
        maksAlderDager: Long?,
        varseltype: String?,
        inkluderStandardtekster: Boolean
    ): List<VarselTekster.DetaljertAntall> {

        val egendefinerteTekster = tellEgendefinerteTekster(teksttype, maksAlderDager, varseltype)

        return if (inkluderStandardtekster && teksttype != Teksttype.WebTekst) {
            egendefinerteTekster + tellStandandardtekster(teksttype, maksAlderDager, varseltype)
        } else {
            egendefinerteTekster
        }.sortedByDescending {
            it.antall
        }
    }

    private fun tellEgendefinerteTeksterTotalt(
        teksttype: Teksttype,
        maksAlderDager: Long?,
        varseltype: String?,
    ): List<VarselTekster.TotaltAntall> {
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
                )
            )
                .map {
                    VarselTekster.TotaltAntall(
                        antall = it.int("antall"),
                        tekst = it.string("tekst")
                    )
                }.asList
        }
    }

    private fun tellEgendefinerteTekster(
        teksttype: Teksttype,
        maksAlderDager: Long?,
        varseltype: String?,
    ): List<VarselTekster.DetaljertAntall> {
        return database.list {
            queryOf("""
                select
                    count(*) as antall,
                    tt.tekst,
                    varsel.event_type as varseltype,
                    varsel.produsent_namespace as namespace,
                    varsel.produsent_appnavn as appnavn
                from
                    varsel join ${teksttype.columnTableName} tt on ${teksttype.columnTableName} = tt.id
                where
                    ((:dato)::timestamp is null or varsel.varseltidspunkt > :dato) and
                    ((:varseltype)::text is null or varsel.event_type = :varseltype)
                group by tt.tekst, varseltype, namespace, appnavn
                order by antall desc
            """,
                mapOf(
                    "dato" to maksAlderDager?.let { LocalDate.now().minusDays(it) },
                    "varseltype" to varseltype
                )
            )
                .map {
                    VarselTekster.DetaljertAntall(
                        varseltype = it.string("varseltype"),
                        produsentNamespace = it.string("namespace"),
                        produsentAppnavn = it.string("appnavn"),
                        antall = it.int("antall"),
                        tekst = it.string("tekst")
                    )
                }.asList
        }
    }

    private fun tellStandandardteksterTotalt(
        teksttype: Teksttype,
        maksAlderDager: Long?,
        varseltype: String?,
    ): VarselTekster.TotaltAntall {

        return database.single {
            queryOf("""
                select
                    count(*) as antall
                from
                    varsel
                where
                    ${teksttype.preferenceColumn} and ${teksttype.columnTableName} is null and
                    ((:dato)::timestamp is null or varsel.varseltidspunkt > :dato) and
                    ((:varseltype)::text is null or varsel.event_type = :varseltype)
                order by antall desc
            """,
                mapOf(
                    "dato" to maksAlderDager?.let { LocalDate.now().minusDays(it) },
                    "varseltype" to varseltype
                )
            )
                .map {
                    VarselTekster.TotaltAntall(
                        antall = it.int("antall"),
                        tekst = "<standardtekst>"
                    )
                }.asSingle
        }
    }

    private fun tellStandandardtekster(
        teksttype: Teksttype,
        maksAlderDager: Long?,
        varseltype: String?,
    ): List<VarselTekster.DetaljertAntall> {

        return database.list {
            queryOf("""
                select
                    count(*) as antall,
                    varsel.event_type as varseltype,
                    varsel.produsent_namespace as namespace,
                    varsel.produsent_appnavn as appnavn
                from
                    varsel
                where
                    ${teksttype.preferenceColumn} and ${teksttype.columnTableName} is null and
                    ((:dato)::timestamp is null or varsel.varseltidspunkt > :dato) and
                    ((:varseltype)::text is null or varsel.event_type = :varseltype)
                group by varseltype, namespace, appnavn
                order by antall desc
            """,
                mapOf(
                    "dato" to maksAlderDager?.let { LocalDate.now().minusDays(it) },
                    "varseltype" to varseltype
                )
            )
                .map {
                    VarselTekster.DetaljertAntall(
                        varseltype = it.string("varseltype"),
                        produsentNamespace = it.string("namespace"),
                        produsentAppnavn = it.string("appnavn"),
                        antall = it.int("antall"),
                        tekst = "<standardtekst>"
                    )
                }.asList
        }
    }
}

enum class Teksttype(val columnTableName: String, val preferenceColumn: String) {
    WebTekst("web_tekst", "N/A"),
    SmsTekst("sms_tekst", "sms_preferert"),
    EpostTittel("epost_tittel", "epost_preferert"),
    EpostTekst("epost_tekst", "epost_preferert");

    companion object {
        fun parse(string: String): Teksttype {
            return entries
                .firstOrNull { it.name.lowercase() == string.lowercase() }
                ?: throw IllegalArgumentException("Fant ikke teksttype for verdi $string")
        }
    }
}
