package no.nav.tms.varseltekst.monitor.coalesce.rules

import no.nav.tms.varseltekst.monitor.coalesce.TekstTable
import no.nav.tms.varseltekst.monitor.util.LocalDateTimeHelper.nowAtUtc
import no.nav.tms.varseltekst.monitor.database.executeBatchUpdateQuery
import no.nav.tms.varseltekst.monitor.database.getUtcDateTime
import no.nav.tms.varseltekst.monitor.database.list
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types


private const val insertRuleQuery = """
    insert into coalescing_rule(name, description, created_at)
        values(?, ?, ?)
"""

private fun insertBacklogQuery(table: TekstTable) = """
    insert into coalescing_backlog(tekst_table, rule_id, tekst_id)
        select '$table', rule.id, tekst.id from $table as tekst
            join coalescing_rule as rule on rule.name = ?
"""

fun Connection.selectCoalescingRules(): List<RuleDto> {
    return prepareStatement("SELECT * FROM coalescing_rule").use {
        it.executeQuery().list {
            toRuleDto()
        }
    }
}


fun Connection.insertCoalescingRules(rules: List<CoalescingRule>) {
    executeBatchUpdateQuery(insertRuleQuery) {
        rules.forEach {
            setString(1, it.name)
            setString(2, it.description)
            setObject(3, nowAtUtc(), Types.TIMESTAMP)

            addBatch()
        }
    }
}

fun Connection.insertIntoBacklog(rule: CoalescingRule, table: TekstTable) {
    prepareStatement(insertBacklogQuery(table)).use {
        it.setString(1, rule.name)
        it.executeUpdate()
    }
}

private fun ResultSet.toRuleDto() =
    RuleDto(
        id = getInt("id"),
        name = getString("name"),
        description = getString("description"),
        createdAt = getUtcDateTime("created_at"),
    )
