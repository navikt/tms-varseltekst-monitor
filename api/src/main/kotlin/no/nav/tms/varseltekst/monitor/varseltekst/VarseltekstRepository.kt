package no.nav.tms.varseltekst.monitor.varseltekst

import kotliquery.Row
import kotliquery.queryOf
import no.nav.tms.varseltekst.monitor.setup.Database
import java.time.LocalDate

class VarseltekstRepository(private val database: Database) {

    fun tellAntallVarselteksterTotalt(
        teksttyper: List<Teksttype>,
        varseltype: String?,
        startDato: LocalDate?,
        sluttDato: LocalDate?,
        inkluderStandardtekster: Boolean
    ): TotaltAntall {

        val queryHelper = DynamicQueryHelper(teksttyper, inkluderStandardtekster)

        return database.list {
            queryOf(
                """
                select
                    count(*) as antall,
                    ${ queryHelper.select }
                from
                    varsel ${ queryHelper.join }
                where
                    ${ queryHelper.where }
                    ${ if (startDato != null) "and varsel.varseltidspunkt > :startDato " else "" }
                    ${ if (sluttDato != null) "and varsel.varseltidspunkt < :sluttDato " else "" }
                    ${ if (varseltype != null) "and varsel.event_type = :varseltype " else "" }
                group by ${ queryHelper.groupBy }
                order by antall desc
            """,
                mapOf(
                    "startDato" to startDato,
                    "sluttDato" to sluttDato,
                    "varseltype" to varseltype
                )
            )
                .map {
                    TotaltAntall.Permutasjon(
                        antall = it.int("antall"),
                        tekster = queryHelper.mapTekster(it)
                    )
                }.asList
        }.let {
            TotaltAntall(teksttyper, it)
        }
    }

    fun tellAntallVarseltekster(
        teksttyper: List<Teksttype>,
        varseltype: String?,
        startDato: LocalDate?,
        sluttDato: LocalDate?,
        inkluderStandardtekster: Boolean
    ): DetaljertAntall {

        val queryHelper = DynamicQueryHelper(teksttyper, inkluderStandardtekster)

        return database.list {
            queryOf("""
                select
                    count(*) as antall,
                    varsel.event_type as varseltype,
                    varsel.produsent_namespace as namespace,
                    varsel.produsent_appnavn as appnavn,
                    ${queryHelper.select}
                from
                    varsel ${ queryHelper.join }
                where
                    ${ queryHelper.where }
                    ${ if (startDato != null) "and varsel.varseltidspunkt > :startDato " else "" }
                    ${ if (sluttDato != null) "and varsel.varseltidspunkt < :sluttDato " else "" }
                    ${ if (varseltype != null) "and varsel.event_type = :varseltype " else "" }
                group by ${queryHelper.groupBy}, varseltype, namespace, appnavn
                order by antall desc
            """,
                mapOf(
                    "startDato" to startDato,
                    "sluttDato" to sluttDato,
                    "varseltype" to varseltype
                )
            )
                .map {
                    DetaljertAntall.Permutasjon(
                        varseltype = it.string("varseltype"),
                        produsent = DetaljertAntall.Produsent(
                            namespace = it.string("namespace"),
                            appnavn = it.string("appnavn")
                        ),
                        antall = it.int("antall"),
                        tekster = queryHelper.mapTekster(it)
                    )
                }.asList
        }.let {
            DetaljertAntall(teksttyper, it)
        }
    }
}

private class DynamicQueryHelper(
    val teksttyper: List<Teksttype>,
    inkluderStandardtekster: Boolean
) {
    val select = teksttyper.mapIndexed { i, _ -> "tt$i.tekst as tekst_$i" }.joinToString()

    val join = teksttyper.mapIndexed { i, teksttype ->
        "left join ${teksttype.columnTableName} as tt$i on varsel.${teksttype.columnTableName} = tt$i.id"
    }.joinToString(" ")

    val where = teksttyper.mapIndexed { i, teksttype ->
        if (inkluderStandardtekster) {
            "((varsel.${teksttype.preferenceColumn} and varsel.${teksttype.columnTableName} is null) or ${teksttype.columnTableName} is not null)"
        } else {
            "varsel.${teksttype.columnTableName} is not null"
        }
    }.joinToString(" ")

    val groupBy = teksttyper.mapIndexed { i, _ -> "tekst_$i" }.joinToString()

    fun mapTekster(row: Row): List<Tekst> {
        return teksttyper.mapIndexed { i, _ ->
            Tekst(row.stringOrNull("tekst_$i"))
        }
    }
}

enum class Teksttype(val columnTableName: String, val preferenceColumn: String) {
    WebTekst("web_tekst", "false"),
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
