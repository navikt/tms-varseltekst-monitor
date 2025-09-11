package no.nav.tms.varseltekst.monitor.coalesce

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tms.varseltekst.monitor.coalesce.rules.RuleDto
import no.nav.tms.varseltekst.monitor.setup.Database
import no.nav.tms.varseltekst.monitor.util.LocalDateTimeHelper.nowAtUtc
import no.nav.tms.varseltekst.monitor.util.singleInTx
import no.nav.tms.varseltekst.monitor.util.singleOrNullInTx
import no.nav.tms.varseltekst.monitor.util.updateInTx

class CoalescingRepository(val database: Database) {
    fun getCoalescingRules(): List<RuleDto> = database.list {
        queryOf("SELECT * FROM coalescing_rule")
            .map(toRuleDto())
            .asList
    }

    fun updateTekst(ruleId: Int, oldTekst: VarselTekst, newTekst: String) = database.transaction {

        val newTekstId = upsertTekst(oldTekst.table, newTekst)

        updateVarselTeksts(oldTekst.table, oldTekst.id, newTekstId)

        updateVarselTekstHistory(oldTekst.table, ruleId, oldTekst.id, newTekstId)
    }

    fun deleteBacklogEntry(id: Int) = database.update {
        queryOf(
            "DELETE FROM coalescing_backlog WHERE id = :backlogId",
            mapOf("backlogId"  to id)
        )
    }

    fun selectTekst(table: TekstTable, id: Int) = database.single {
        queryOf(
            "SELECT * FROM $table WHERE id = :tekstId",
            mapOf("tekstId" to id)
        )
            .map(toVarselTekst(table))
            .asSingle
    }

    private fun toRuleDto(): (Row) -> RuleDto = { row ->
        RuleDto(
            id = row.int("id"),
            name = row.string("name"),
            description = row.string("description"),
            createdAt = row.localDateTime("created_at"),
        )
    }

    private fun toVarselTekst(tekstTable: TekstTable): (Row) -> VarselTekst = {
        VarselTekst(
            table = tekstTable,
            tekst = it.string("tekst"),
            id = it.int("id")
        )
    }

    private fun TransactionalSession.upsertTekst(table: TekstTable, tekst: String): Int {
        val existingId = existingTekstId(table, tekst)

        return if (existingId == null) {
            insertTekst(table, tekst)
        } else {
            existingId
        }
    }

    private fun TransactionalSession.existingTekstId(table: TekstTable, tekst: String): Int? = singleOrNullInTx {
        queryOf(
            "select id from $table where tekst = :tekst",
            mapOf("tekst" to tekst)
        )
            .map { it.int("id") }
            .asSingle
    }

    private fun TransactionalSession.insertTekst(table: TekstTable, tekst: String): Int = singleInTx {
        queryOf("""
            INSERT INTO $table(tekst, first_seen_at) VALUES(:tekst, :firstSeenAt)
                 RETURNING id
            """,
            mapOf(
                "tekst" to tekst,
                "firstSeenAt" to nowAtUtc()
            )
        )
            .map { it.int("id") }
            .asSingle
    }

    private fun TransactionalSession.updateVarselTeksts(table: TekstTable, oldId: Int, newId: Int) = updateInTx {
        queryOf(
            "UPDATE varsel SET $table = :newId WHERE $table = :oldId",
            mapOf(
                "oldId" to oldId,
                "newId" to newId
            )
        )
    }

    private fun insertHistoryQuery(table: TekstTable) = """
        INSERT INTO coalescing_history_$table(rule_id, old_tekst_id, new_tekst_id, applied_at)
            VALUES(:ruleId, :oldId, :newId, :appliedAt)
    """

    private fun TransactionalSession.updateVarselTekstHistory(table: TekstTable, ruleId: Int, oldId: Int, newId: Int) = updateInTx {
        queryOf(
            insertHistoryQuery(table),
            mapOf(
                "ruleId" to ruleId,
                "oldId" to oldId,
                "newId" to newId,
                "appliedAt" to nowAtUtc()
            )
        )
    }
}
