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
                teksttype = request.teksttype,
                varseltype = request.varseltype,
                startDato = request.startDato,
                sluttDato = request.sluttDato,
                inkluderStandardtekster = request.inkluderStandardtekster
            ).let {
                ExcelFileWriter.antallToExcelSheet(it, request.teksttype, request.minimumAntall)
            }
        } else {
            varseltekstRepository.tellAntallVarselteksterTotalt(
                teksttype = request.teksttype,
                varseltype = request.varseltype,
                startDato = request.startDato,
                sluttDato = request.sluttDato,
                inkluderStandardtekster = request.inkluderStandardtekster
            ).let {
                ExcelFileWriter.totaltAntallToExcelSheet(it, request.teksttype, request.minimumAntall)
            }
        }
    }

}
