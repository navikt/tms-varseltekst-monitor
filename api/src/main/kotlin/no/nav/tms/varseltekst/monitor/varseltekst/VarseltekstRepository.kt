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

        val egendefinerteTekster = tellEgendefinerteTeksterTotalt(teksttype, varseltype, startDato, sluttDato)

        return if (inkluderStandardtekster && teksttype != Teksttype.WebTekst) {
            egendefinerteTekster + tellStandandardteksterTotalt(teksttype, varseltype, startDato, sluttDato)
        } else {
            egendefinerteTekster
        }.sortedByDescending {
            it.antall
        }
    }

    fun tellAntallVarseltekster(
        teksttype: Teksttype,
        varseltype: String?,
        startDato: LocalDate?,
        sluttDato: LocalDate?,
        inkluderStandardtekster: Boolean
    ): List<VarselTekster.DetaljertAntall> {

        val egendefinerteTekster = tellEgendefinerteTekster(teksttype, varseltype, startDato, sluttDato)

        return if (inkluderStandardtekster && teksttype != Teksttype.WebTekst) {
            egendefinerteTekster + tellStandandardtekster(teksttype, varseltype, startDato, sluttDato)
        } else {
            egendefinerteTekster
        }.sortedByDescending {
            it.antall
        }
    }

    private fun tellEgendefinerteTeksterTotalt(
        teksttype: Teksttype,
        varseltype: String?,
        startDato: LocalDate?,
        sluttDato: LocalDate?
    ): List<VarselTekster.TotaltAntall> {
        return database.list {
            queryOf("""
                select
                    count(*) as antall,
                    tt.tekst
                from
                    varsel join ${teksttype.columnTableName} tt on ${teksttype.columnTableName} = tt.id
                where
                    ((:startDato)::timestamp is null or varsel.varseltidspunkt > :startDato) and
                    ((:sluttDato)::timestamp is null or varsel.varseltidspunkt < :sluttDato) and
                    ((:varseltype)::text is null or varsel.event_type = :varseltype)
                group by tt.tekst
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
                        tekst = it.string("tekst")
                    )
                }.asList
        }
    }

    private fun tellEgendefinerteTekster(
        teksttype: Teksttype,
        varseltype: String?,
        startDato: LocalDate?,
        sluttDato: LocalDate?
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
                    ((:startDato)::timestamp is null or varsel.varseltidspunkt > :startDato) and
                    ((:sluttDato)::timestamp is null or varsel.varseltidspunkt < :sluttDato) and
                    ((:varseltype)::text is null or varsel.event_type = :varseltype)
                group by tt.tekst, varseltype, namespace, appnavn
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
                        tekst = it.string("tekst")
                    )
                }.asList
        }
    }

    private fun tellStandandardteksterTotalt(
        teksttype: Teksttype,
        varseltype: String?,
        startDato: LocalDate?,
        sluttDato: LocalDate?,
    ): VarselTekster.TotaltAntall {

        return database.single {
            queryOf("""
                select
                    count(*) as antall
                from
                    varsel
                where
                    ${teksttype.preferenceColumn} and ${teksttype.columnTableName} is null and
                    ((:startDato)::timestamp is null or varsel.varseltidspunkt > :startDato) and
                    ((:sluttDato)::timestamp is null or varsel.varseltidspunkt < :sluttDato) and
                    ((:varseltype)::text is null or varsel.event_type = :varseltype)
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
                        tekst = null
                    )
                }.asSingle
        }
    }

    private fun tellStandandardtekster(
        teksttype: Teksttype,
        varseltype: String?,
        startDato: LocalDate?,
        sluttDato: LocalDate?
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
                    ((:startDato)::timestamp is null or varsel.varseltidspunkt > :startDato) and
                    ((:sluttDato)::timestamp is null or varsel.varseltidspunkt < :sluttDato) and
                    ((:varseltype)::text is null or varsel.event_type = :varseltype)
                group by varseltype, namespace, appnavn
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
                        tekst = null
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
