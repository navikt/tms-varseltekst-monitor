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
        harEksternVarsling: Boolean?,
        inkluderStandardtekster: Boolean,
        inkluderUbrukteKanaler: Boolean
    ): TotaltAntall {

        val queryHelper = DynamicQueryHelper(teksttyper, inkluderStandardtekster, inkluderUbrukteKanaler)

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
                    ${ if (varseltype != null) "and varsel.event_type = :varseltype " else "" }
                    ${ if (startDato != null) "and varsel.varseltidspunkt > :startDato " else "" }
                    ${ if (sluttDato != null) "and varsel.varseltidspunkt < :sluttDato " else "" }
                    ${ if (harEksternVarsling != null) "and varsel.eksternVarsling = :eksternVarsling " else "" }
                group by ${ queryHelper.groupBy }
                order by antall desc
            """,
                mapOf(
                    "startDato" to startDato,
                    "sluttDato" to sluttDato,
                    "varseltype" to varseltype,
                    "eksternVarsling" to harEksternVarsling
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
        harEksternVarsling: Boolean?,
        inkluderStandardtekster: Boolean,
        inkluderUbrukteKanaler: Boolean = false
    ): DetaljertAntall {

        val queryHelper = DynamicQueryHelper(teksttyper, inkluderStandardtekster, inkluderUbrukteKanaler)

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
                    ${ if (varseltype != null) "and varsel.event_type = :varseltype " else "" }
                    ${ if (startDato != null) "and varsel.varseltidspunkt > :startDato " else "" }
                    ${ if (sluttDato != null) "and varsel.varseltidspunkt < :sluttDato " else "" }
                    ${ if (harEksternVarsling != null) "and varsel.eksternVarsling = :eksternVarsling " else "" }
                group by ${queryHelper.groupBy}, varseltype, namespace, appnavn
                order by antall desc
            """,
                mapOf(
                    "startDato" to startDato,
                    "sluttDato" to sluttDato,
                    "varseltype" to varseltype,
                    "eksternVarsling" to harEksternVarsling
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
    inkluderStandardtekster: Boolean,
    inkluderUbrukteKanaler: Boolean
) {
    val select = teksttyper.mapIndexed { i, teksttype ->
        "tt$i.tekst as tekst_$i, (${preferenceColumn(teksttype)} and varsel.${columnTableName(teksttype)} is null) as standardtekst_$i"
    }.joinToString()

    val join = teksttyper.mapIndexed { i, teksttype ->
        "left join ${columnTableName(teksttype)} as tt$i on varsel.${columnTableName(teksttype)} = tt$i.id"
    }.joinToString(" ")

    val where = teksttyper.mapIndexed { i, teksttype ->
        if (inkluderStandardtekster && inkluderUbrukteKanaler) {
            "true"
        } else if (inkluderUbrukteKanaler) {
            "not (${preferenceColumn(teksttype)} and varsel.${columnTableName(teksttype)} is null)"
        } else if (inkluderStandardtekster) {
            "((${preferenceColumn(teksttype)} and varsel.${columnTableName(teksttype)} is null) or ${columnTableName(teksttype)} is not null)"
        } else {
            "varsel.${columnTableName(teksttype)} is not null"
        }
    }.joinToString(" and ")

    val groupBy = teksttyper.mapIndexed { i, _ -> "tekst_$i, standardtekst_$i" }.joinToString()

    fun mapTekster(row: Row): List<Tekst> {
        return teksttyper.mapIndexed { i, _ ->
            val tekst = row.stringOrNull("tekst_$i")

            Tekst(
                tekst = tekst,
                innhold = if (tekst != null) {
                    Tekst.Innhold.Egendefinert
                } else if (row.boolean("standardtekst_$i")){
                    Tekst.Innhold.Standard
                } else {
                    Tekst.Innhold.Ingen
                }
            )
        }
    }

    private fun columnTableName(teksttype: Teksttype) = when(teksttype) {
        Teksttype.WebTekst -> "web_tekst"
        Teksttype.SmsTekst -> "sms_tekst"
        Teksttype.EpostTittel -> "epost_tittel"
        Teksttype.EpostTekst -> "epost_tekst"
    }

    private fun preferenceColumn(teksttype: Teksttype) = when(teksttype) {
        Teksttype.WebTekst -> "false"
        Teksttype.SmsTekst -> "sms_preferert"
        Teksttype.EpostTittel -> "epost_preferert"
        Teksttype.EpostTekst -> "epost_preferert"
    }
}

