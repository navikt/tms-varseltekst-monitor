package no.nav.tms.varseltekst.monitor.varseltekst

import kotliquery.queryOf
import no.nav.tms.varseltekst.monitor.setup.Database
import java.time.LocalDate

class VarseltekstRepository(private val database: Database) {

    fun tellAntallVarselteksterTotalt(
        teksttype: Teksttype,
        varseltype: String?,
        startDato: LocalDate?,
        sluttDato: LocalDate?,
        inkluderStandardtekster: Boolean
    ): List<VarselTekster.TotaltAntall> {

        val queryHelper = DynamicQueryHelper(listOf(teksttype), inkluderStandardtekster)

        return database.list {
            queryOf(
                """
                select
                    count(*) as antall,
                    ${ queryHelper.columns }
                from
                    varsel ${ queryHelper.join }
                where
                    ${ queryHelper.where }
                    ${ if (startDato != null) "and varsel.varseltidspunkt > :startDato " else "" }
                    ${ if (sluttDato != null) "and varsel.varseltidspunkt < :sluttDato " else "" }
                    ${ if (varseltype != null) "and varsel.event_type = :varseltype " else "" }
                group by ${ queryHelper.columns }
                order by antall desc
            """,
                mapOf(
                    "startDato" to startDato,
                    "sluttDato" to sluttDato,
                    "varseltype" to varseltype
                )
            )
                .map {
                    VarselTekster.TotaltAntall(
                        antall = it.int("antall"),
                        tekst = it.stringOrNull("tekst")
                    )
                }.asList
        }
    }

    fun tellAntallVarseltekster(
        teksttype: Teksttype,
        varseltype: String?,
        startDato: LocalDate?,
        sluttDato: LocalDate?,
        inkluderStandardtekster: Boolean
    ): List<VarselTekster.DetaljertAntall> {

        val queryHelper = DynamicQueryHelper(listOf(teksttype), inkluderStandardtekster)

        return database.list {
            queryOf("""
                select
                    count(*) as antall,
                    varsel.event_type as varseltype,
                    varsel.produsent_namespace as namespace,
                    varsel.produsent_appnavn as appnavn,
                    ${queryHelper.columns}
                from
                    varsel ${ queryHelper.join }
                where
                    ${ queryHelper.where }
                    ${ if (startDato != null) "and varsel.varseltidspunkt > :startDato " else "" }
                    ${ if (sluttDato != null) "and varsel.varseltidspunkt < :sluttDato " else "" }
                    ${ if (varseltype != null) "and varsel.event_type = :varseltype " else "" }
                group by ${queryHelper.columns}, varseltype, namespace, appnavn
                order by antall desc
            """,
                mapOf(
                    "startDato" to startDato,
                    "sluttDato" to sluttDato,
                    "varseltype" to varseltype
                )
            )
                .map {
                    VarselTekster.DetaljertAntall(
                        varseltype = it.string("varseltype"),
                        produsent = VarselTekster.Produsent(
                            namespace = it.string("namespace"),
                            appnavn = it.string("appnavn")
                        ),
                        antall = it.int("antall"),
                        tekst = it.stringOrNull("tekst")
                    )
                }.asList
        }
    }
}

private class DynamicQueryHelper(
    teksttyper: List<Teksttype>,
    inkluderStandardtekster: Boolean
) {

    val columns = teksttyper.mapIndexed { i, _ -> "tt$i.tekst" }.joinToString()

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
