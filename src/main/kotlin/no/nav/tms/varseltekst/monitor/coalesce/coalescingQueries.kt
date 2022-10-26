package no.nav.tms.varseltekst.monitor.coalesce

import no.nav.tms.varseltekst.monitor.util.LocalDateTimeHelper.nowAtUtc
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types

private val selectBacklogEntryQuery = """
    SELECT cb.* FROM coalescing_backlog as cb
        join coalescing_rule as cr on cb.rule_id = cr.id 
    ORDER BY cr.created_at, cr.id
    LIMIT 1
""".trimIndent()

private fun upsertTekstQuery(table: TekstTable) = """
    WITH input_tekst(tekst) AS (
        VALUES(?)
    ),
         inserted_tekst AS (
             INSERT INTO $table(tekst) SELECT tekst FROM input_tekst
                 ON CONFLICT (tekst) DO NOTHING
             RETURNING id
         )
    SELECT id FROM inserted_tekst
        UNION ALL
    SELECT t.id FROM input_tekst 
        JOIN $table t USING (tekst);
""".trimIndent()

private fun insertHistoryQuery(table: TekstTable) = """
    INSERT INTO coalescing_history_$table(rule_id, old_tekst_id, new_tekst_id, applied_at)
        VALUES(?, ?, ?, ?)
""".trimIndent()

fun Connection.selectSingleBacklogEntry(): BacklogEntry? {
    return prepareStatement(selectBacklogEntryQuery).use {
        it.executeQuery().let { result ->
            if (result.next()) {
                result.toBacklogEntry()
            } else {
                null
            }
        }
    }
}

fun Connection.deleteBacklogEntryById(id: Int) {
    prepareStatement("DELETE FROM coalescing_backlog WHERE id = ?").use {
        it.setInt(1, id)

        it.executeUpdate()
    }
}

fun Connection.selectTekstById(table: TekstTable, id: Int): VarselTekst {
    return prepareStatement("SELECT * FROM $table WHERE id = ?").use {
        it.setInt(1, id)

        it.executeQuery().let { result ->

            if (result.next()) {
                result.toVarselTekst(table)
            } else {
                throw IllegalStateException("Fant ikke tekst i db.")
            }
        }
    }
}

fun Connection.upsertTekst(table: TekstTable, tekst: String): Int {
    return prepareStatement(upsertTekstQuery(table)).use {
        it.setString(1, tekst)

        it.executeQuery().let { result ->
            if (result.next()) {
                result.getInt("id")
            } else {
                throw IllegalStateException("Fant ikke tekst i db.")
            }
        }
    }
}

fun Connection.updateVarselTeksts(table: TekstTable, oldId: Int, newId: Int) {
    prepareStatement("UPDATE varsel SET $table = ? WHERE $table = ?").use {
        it.setInt(1, newId)
        it.setInt(2, oldId)

        it.executeUpdate()
    }
}

fun Connection.updateVarselTekstHistory(table: TekstTable, ruleId: Int, oldId: Int, newId: Int) {
    prepareStatement(insertHistoryQuery(table)).use {
        it.setInt(1, ruleId)
        it.setInt(2, oldId)
        it.setInt(3, newId)
        it.setObject(4, nowAtUtc(), Types.TIMESTAMP)

        it.executeUpdate()
    }
}

private fun ResultSet.toBacklogEntry() =
    BacklogEntry(
        id = getInt("id"),
        tekstTable = TekstTable.valueOf(getString("tekst_table")),
        ruleId = getInt("rule_id"),
        tekstId = getInt("tekst_id")
    )

private fun ResultSet.toVarselTekst(tekstTable: TekstTable) =
    VarselTekst(
        table = tekstTable,
        tekst = getString("tekst"),
        id = getInt("id")
    )
