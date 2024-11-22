package no.nav.tms.varseltekst.monitor.coalesce

import kotliquery.*
import no.nav.tms.varseltekst.monitor.coalesce.rules.*
import no.nav.tms.varseltekst.monitor.setup.Database
import no.nav.tms.varseltekst.monitor.util.updateInTx
import no.nav.tms.varseltekst.monitor.util.LocalDateTimeHelper.nowAtUtc


class BacklogRepository(private val database: Database) {
    fun persistRulesAndBacklog(rules: List<CoalescingRule>) = database.transaction {
        insertCoalescingRules(rules)

        fillBacklog(rules)
    }

    fun getNextBacklogEntries(batchSize: Int) = database.list {
        queryOf(
            """
                SELECT cb.* FROM coalescing_backlog as cb
                    join coalescing_rule as cr on cb.rule_id = cr.id 
                ORDER BY cr.created_at, cr.id
                LIMIT :batch
            """,
            mapOf("batch" to batchSize)
        )
            .map(toBacklogEntry())
            .asList
    }

    private fun TransactionalSession.insertCoalescingRules(rules: List<CoalescingRule>) {
        batchPreparedNamedStatement(
            "insert into coalescing_rule(name, description, created_at) values(:name, :description, :createdAt)",
            rules.map {
                mapOf(
                    "name" to it.name,
                    "description" to it.description,
                    "createdAt" to nowAtUtc()
                )
            }
        )
    }

    private fun TransactionalSession.fillBacklog(rules: List<CoalescingRule>) {
        rules.forEach { rule ->
            TekstTable.values().forEach { table ->
                insertIntoBacklog(rule, table)
            }
        }
    }

    private fun insertBacklogQuery(table: TekstTable) = """
        insert into coalescing_backlog(tekst_table, rule_id, tekst_id)
            select '$table', rule.id, tekst.id from $table as tekst
                join coalescing_rule as rule on rule.name = :rule
    """

    fun TransactionalSession.insertIntoBacklog(rule: CoalescingRule, table: TekstTable) = updateInTx {
        queryOf(
            insertBacklogQuery(table),
            mapOf("rule" to rule.name)
        )
    }

    private fun toBacklogEntry(): (Row) -> BacklogEntry = {
        BacklogEntry(
            id = it.int("id"),
            tekstTable = TekstTable.valueOf(it.string("tekst_table")),
            ruleId = it.int("rule_id"),
            tekstId = it.int("tekst_id")
        )
    }
}
