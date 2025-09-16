package no.nav.tms.varseltekst.monitor.varseltekst

import kotlinx.coroutines.*
import org.apache.poi.ss.usermodel.Workbook

class VarselDownloadQueryHandler(
    private val varseltekstRepository: VarseltekstRepository
) {
    private val queryScope = CoroutineScope(Dispatchers.IO + Job())

    fun startQuery(request: DownloadRequest): Deferred<Workbook> = queryScope.async {
        if (request.detaljert) {
            varseltekstRepository.tellAntallVarseltekster(
                teksttyper = request.teksttyper,
                varseltype = request.varseltype,
                startDato = request.startDato,
                sluttDato = request.sluttDato,
                inkluderStandardtekster = request.inkluderStandardtekster
            ).let {
                ExcelFileWriter.antallToExcelSheet(it, request.teksttyper, request.minimumAntall)
            }
        } else {
            varseltekstRepository.tellAntallVarselteksterTotalt(
                teksttyper = request.teksttyper,
                varseltype = request.varseltype,
                startDato = request.startDato,
                sluttDato = request.sluttDato,
                inkluderStandardtekster = request.inkluderStandardtekster
            ).let {
                ExcelFileWriter.totaltAntallToExcelSheet(it, request.teksttyper, request.minimumAntall)
            }
        }
    }

}
