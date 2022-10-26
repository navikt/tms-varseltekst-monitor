package no.nav.tms.varseltekst.monitor.database

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.LocalDateTime

fun <T> ResultSet.list(result: ResultSet.() -> T): List<T> =
    mutableListOf<T>().apply {
        while (next()) {
            add(result())
        }
    }

fun ResultSet.getUtcDateTime(columnLabel: String): LocalDateTime = getTimestamp(columnLabel).toLocalDateTime()

fun Connection.executeBatchUpdateQuery(sql: String, paramInit: PreparedStatement.() -> Unit) {
    autoCommit = false
    prepareStatement(sql).use { statement ->
        statement.paramInit()
        statement.executeBatch()
    }
    commit()
}
