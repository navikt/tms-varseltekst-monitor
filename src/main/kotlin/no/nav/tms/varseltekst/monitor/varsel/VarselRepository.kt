package no.nav.tms.varseltekst.monitor.varsel

import no.nav.tms.varseltekst.monitor.common.database.Database

class VarselRepository(private val database: Database) {
    fun persistVarselTekster(varseltekster: Varseltekster) = database.queryWithExceptionTranslation {
        createVarseltekster(varseltekster)
    }
}
