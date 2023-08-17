package no.nav.tms.varseltekst.monitor.database

import kotliquery.Query
import kotliquery.TransactionalSession
import kotliquery.action.NullableResultQueryAction

fun TransactionalSession.updateInTx(queryBuilder: () -> Query) = run(queryBuilder.invoke().asUpdate)

fun <T> TransactionalSession.singleOrNullInTx(queryBuilder: () -> NullableResultQueryAction<T>) = run(queryBuilder.invoke())

fun <T> TransactionalSession.singleInTx(queryBuilder: () -> NullableResultQueryAction<T>) = singleOrNullInTx(queryBuilder)
    ?: throw IllegalStateException("Found no rows matching query")
