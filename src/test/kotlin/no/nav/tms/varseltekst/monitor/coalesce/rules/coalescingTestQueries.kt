package no.nav.tms.varseltekst.monitor.coalesce.rules

import kotliquery.queryOf
import no.nav.tms.varseltekst.monitor.coalesce.TekstTable
import no.nav.tms.varseltekst.monitor.setup.Database
import no.nav.tms.varseltekst.monitor.util.LocalDateTimeHelper

fun Database.countBackLog(tekstTable: TekstTable) = single {
    queryOf(
        "SELECT COUNT(*) as number_in_backlog FROM coalescing_backlog WHERE tekst_table = :table",
        mapOf("table" to tekstTable.name)
    )
        .map { it.int("number_in_backlog") }
        .asSingle
}

fun Database.insertRule(rule: CoalescingRule): RuleDto {
    val time = LocalDateTimeHelper.nowAtUtc()

    val id = single {
        queryOf("""
        INSERT INTO coalescing_rule(name, description, created_at) 
            VALUES (:name, :description, :createdAt)
            RETURNING id
        """,
            mapOf(
                "name" to rule.name,
                "description" to rule.description,
                "createdAt" to time
            )
        )
            .map { it.int("id") }
            .asSingle
    }

    return RuleDto(
        id,
        rule.name,
        rule.description,
        time
    )
}

