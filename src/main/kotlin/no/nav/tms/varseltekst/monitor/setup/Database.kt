package no.nav.tms.varseltekst.monitor.setup

import com.zaxxer.hikari.HikariDataSource
import kotliquery.Query
import kotliquery.TransactionalSession
import kotliquery.action.ListResultQueryAction
import kotliquery.action.NullableResultQueryAction
import kotliquery.sessionOf
import kotliquery.using

interface Database {

    val dataSource: HikariDataSource

    fun update(queryBuilder: () -> Query) {
        using(sessionOf(dataSource)) {
            it.run(queryBuilder.invoke().asUpdate)
        }
    }

    fun <T> singleOrNull(action: () -> NullableResultQueryAction<T>): T? =
        using(sessionOf(dataSource)) {
            it.run(action.invoke())
        }

    fun <T> single(action: () -> NullableResultQueryAction<T>): T =
        using(sessionOf(dataSource)) {
            it.run(action.invoke())
        } ?: throw IllegalStateException("Found no rows matching query")

    fun <T> list(action: () -> ListResultQueryAction<T>): List<T> =
        using(sessionOf(dataSource)) {
            it.run(action.invoke())
        }

    fun <T> transaction(actions: TransactionalSession.() -> T): T {
        val session = sessionOf(dataSource)

        val result: T = session.transaction {
            it.actions()
        }

        session.connection.close()

        return result
    }


}
