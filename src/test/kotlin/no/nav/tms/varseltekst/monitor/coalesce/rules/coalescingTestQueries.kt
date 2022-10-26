package no.nav.tms.varseltekst.monitor.coalesce.rules

import no.nav.tms.varseltekst.monitor.coalesce.TekstTable
import no.nav.tms.varseltekst.monitor.util.LocalDateTimeHelper
import java.sql.Connection
import java.sql.Statement
import java.sql.Types

fun Connection.countBackLog(tekstTable: TekstTable): Int {
    prepareStatement("SELECT COUNT(*) as number_in_backlog FROM coalescing_backlog WHERE tekst_table = ?").use {
        it.setString(1, tekstTable.name)

        val result = it.executeQuery()

        return if (result.next()) {
            result.getInt("number_in_backlog")
        } else {
            0
        }
    }
}

fun Connection.insertRule(rule: CoalescingRule): RuleDto {
    prepareStatement("""
        INSERT INTO coalescing_rule(name, description, created_at) 
            VALUES (?, ?, ?)
            RETURNING id
        """, Statement.RETURN_GENERATED_KEYS).use {

            val time = LocalDateTimeHelper.nowAtUtc()

            it.setString(1, rule.name)
            it.setString(2, rule.description)
            it.setObject(3, time, Types.TIMESTAMP)

            val id = it.executeUpdate()

        return RuleDto(
            id,
            rule.name,
            rule.description,
            time
        )
    }
}

