package no.nav.tms.varseltekst.monitor.config

import no.nav.tms.varseltekst.monitor.common.database.Database
import no.nav.tms.varseltekst.monitor.varsel.VarselRepository
import no.nav.tms.varseltekst.monitor.varsel.VarselSink
import no.nav.tms.varseltekst.monitor.varsel.VarselSink2

class ApplicationContext {

    val environment = Environment()
    val database: Database = PostgresDatabase(environment)
    private val varselRepository = VarselRepository(database)
    val varselSink = VarselSink(varselRepository)
    val varselSink2 = VarselSink2(varselRepository)
}

