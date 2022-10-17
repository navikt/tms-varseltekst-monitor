package no.nav.tms.varseltekst.monitor.common.database

import com.zaxxer.hikari.HikariDataSource
import no.nav.tms.varseltekst.monitor.common.exceptions.RetriableDatabaseException
import no.nav.tms.varseltekst.monitor.common.exceptions.UnretriableDatabaseException
import no.nav.tms.varseltekst.monitor.health.HealthCheck
import no.nav.tms.varseltekst.monitor.health.HealthStatus
import no.nav.tms.varseltekst.monitor.health.Status
import org.slf4j.Logger
import java.sql.*

interface Database: HealthCheck {

    val log: Logger

    val dataSource: HikariDataSource

    fun <T> dbQuery(operationToExecute: Connection.() -> T): T {
        return dataSource.connection.use { openConnection ->
            try {
                openConnection.operationToExecute().apply {
                    openConnection.commit()
                }

            } catch (e: Exception) {
                try {
                    openConnection.rollback()
                } catch (rollbackException: Exception) {
                    e.addSuppressed(rollbackException)
                }
                throw e
            }
        }
    }


    fun <T> queryWithExceptionTranslation(operationToExecute: Connection.() -> T): T {
        return translateExternalExceptionsToInternalOnes {
            dbQuery {
                operationToExecute()
            }
        }
    }

    override fun status(): HealthStatus {
        val serviceName = "Database"
        return try {
            dbQuery { prepareStatement("""SELECT * FROM beskjed LIMIT 1""").execute() }
            HealthStatus(serviceName, Status.OK, "200 OK")
        } catch (e: Exception) {
            log.error("Selftest mot databasen feilet", e)
            HealthStatus(serviceName, Status.ERROR, "Feil mot DB")
        }
    }
}

inline fun <T> translateExternalExceptionsToInternalOnes(databaseActions: () -> T): T {
    return try {
        databaseActions()

    } catch (te: SQLTransientException) {
        val message = "Skriving til databasen feilet grunnet en periodisk feil."
        throw RetriableDatabaseException(message, te)

    } catch (re: SQLRecoverableException) {
        val message = "Skriving til databasen feilet grunnet en periodisk feil."
        throw RetriableDatabaseException(message, re)

    } catch (se: SQLException) {
        val message = "Det skjedde en SQL relatert feil ved skriving til databasen."
        val ure = UnretriableDatabaseException(message, se)
        throw ure

    } catch (e: Exception) {
        val message = "Det skjedde en ukjent feil ved skriving til databasen."
        throw UnretriableDatabaseException(message, e)
    }
}
