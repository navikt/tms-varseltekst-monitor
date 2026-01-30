package no.nav.tms.varseltekst.monitor.setup

import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import no.nav.tms.common.postgres.Postgres
import no.nav.tms.common.postgres.PostgresDatabase
import org.flywaydb.core.Flyway
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.postgresql.PostgreSQLContainer

object LocalPostgresDatabase {

    private val container = PostgreSQLContainer("postgres:14.5").apply {
        waitingFor(Wait.forListeningPort())
        start()
    }

    private val instance by lazy {
        Postgres.connectToContainer(container).also {
            // Workaround to fix migration involving missing user.
            it.update {
                queryOf("create user cloudsqliamuser")
            }

            migrate(it.dataSource)
        }
    }

    fun cleanDb(): PostgresDatabase {
        instance.clearAllTables()

        return instance
    }

    private fun migrate(dataSource: HikariDataSource) {

        Flyway.configure()
            .connectRetries(3)
            .dataSource(dataSource)
            .load()
            .migrate()
    }
}
