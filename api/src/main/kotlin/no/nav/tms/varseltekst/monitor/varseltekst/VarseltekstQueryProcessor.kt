package no.nav.tms.varseltekst.monitor.varseltekst

import org.apache.poi.ss.usermodel.Workbook

class VarseltekstQueryProcessor(
    private val varseltekstRepository: VarseltekstRepository
) {

    fun processRequest(request: DownloadRequest): Workbook {
        return if (request.detaljert) {
            varseltekstRepository.tellAntallVarseltekster(
                teksttyper = request.teksttyper,
                varseltype = request.varseltype,
                startDato = request.startDato,
                sluttDato = request.sluttDato,
                inkluderStandardtekster = request.inkluderStandardtekster
            ).let {
                sladdDetaljerteTekster(it, request.minimumAntall)
            }.let {
                ExcelWorkbookWriter.antallToExcelSheet(it)
            }
        } else {
            varseltekstRepository.tellAntallVarselteksterTotalt(
                teksttyper = request.teksttyper,
                varseltype = request.varseltype,
                startDato = request.startDato,
                sluttDato = request.sluttDato,
                inkluderStandardtekster = request.inkluderStandardtekster,
                inkluderUbrukteKanaler = request.inkluderUbrukteKanaler
            ).let {
                sladdTekster(it, request.minimumAntall)
            }.let {
                ExcelWorkbookWriter.totaltAntallToExcelSheet(it)
            }
        }
    }

    private fun sladdTekster(totaltAntall: TotaltAntall, minAntall: Int): TotaltAntall {

        val (beholdes, sladdes) = totaltAntall.permutasjoner.partition {
            permutasjon -> permutasjon.antall >= minAntall
                || permutasjon.tekster.none { it.innhold == Tekst.Innhold.Egendefinert }
        }

        val sladdet = sladdes.map { permutasjon ->
            TotaltAntall.Permutasjon(
                antall = permutasjon.antall,
                tekster = permutasjon.tekster.map { tekst ->
                    if (tekst.innhold == Tekst.Innhold.Egendefinert) {
                        Tekst(null, Tekst.Innhold.Sladdet)
                    } else {
                        tekst
                    }
                }
            )
        }.groupBy {
            it.tekster
        }.map { (tekster, permutasjoner) ->
            TotaltAntall.Permutasjon(
                antall = permutasjoner.sumOf { it.antall },
                tekster = tekster
            )
        }

        return (sladdet + beholdes)
            .sortedByDescending { it.antall }
            .let { TotaltAntall(totaltAntall.teksttyper, it) }
    }


    private fun sladdDetaljerteTekster(detaljertAntall: DetaljertAntall, minAntall: Int): DetaljertAntall {

        val (beholdes, sladdes) = detaljertAntall.permutasjoner.partition { permutasjon ->
            permutasjon.antall >= minAntall
                || permutasjon.tekster.none {it.innhold == Tekst.Innhold.Egendefinert }
        }

        val sladdet = sladdes.map { permutasjon ->
            DetaljertAntall.Permutasjon(
                varseltype = permutasjon.varseltype,
                produsent = permutasjon.produsent,
                antall = permutasjon.antall,
                tekster = permutasjon.tekster.map { tekst ->
                    if (tekst.innhold == Tekst.Innhold.Egendefinert) {
                        Tekst(null, Tekst.Innhold.Sladdet)
                    } else {
                        tekst
                    }
                }
            )
        }.groupBy {
            Triple(it.varseltype, it.produsent, it.tekster)
        }.map { (vpt, permutasjoner) ->
            DetaljertAntall.Permutasjon(
                varseltype = vpt.first,
                produsent = vpt.second,
                antall = permutasjoner.sumOf { it.antall },
                tekster = vpt.third
            )
        }


        return (sladdet + beholdes)
            .sortedByDescending { it.antall }
            .let { DetaljertAntall(detaljertAntall.teksttyper, it) }
    }
}

class FileNotFoundException(val fileId: String): IllegalArgumentException()
class FileNotReadyException(val fileId: String): IllegalArgumentException()
