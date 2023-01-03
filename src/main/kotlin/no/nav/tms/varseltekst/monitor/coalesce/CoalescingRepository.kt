package no.nav.tms.varseltekst.monitor.coalesce

import no.nav.tms.varseltekst.monitor.coalesce.rules.*
import no.nav.tms.varseltekst.monitor.database.Database

class CoalescingRepository(val database: Database) {
    fun getCoalescingRules(): List<RuleDto> = database.queryWithExceptionTranslation {
        selectCoalescingRules()
    }

    fun persistRulesAndBacklog(rules: List<CoalescingRule>) = database.queryWithExceptionTranslation {
        insertCoalescingRules(rules)

        rules.forEach { rule ->
            TekstTable.values().forEach { table ->
                insertIntoBacklog(rule, table)
            }
        }
    }

    fun getNextBacklogEntry(): BacklogEntry? = database.queryWithExceptionTranslation {
        selectSingleBacklogEntry()
    }

    fun deleteBacklogEntry(id: Int) = database.queryWithExceptionTranslation {
        deleteBacklogEntryById(id)
    }

    fun selectTekst(table: TekstTable, id: Int) = database.queryWithExceptionTranslation {
        selectTekstById(table, id)
    }

    fun updateTekst(ruleId: Int, oldTekst: VarselTekst, newTekst: String) {
        database.queryWithExceptionTranslation {

            val newTekstId = upsertTekst(oldTekst.table, newTekst)

            updateVarselTeksts(oldTekst.table, oldTekst.id, newTekstId)

            updateVarselTekstHistory(oldTekst.table, ruleId, oldTekst.id, newTekstId)
        }
    }
}
