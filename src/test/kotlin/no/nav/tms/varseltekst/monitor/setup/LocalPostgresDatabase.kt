package no.nav.tms.varseltekst.monitor.setup

import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import org.flywaydb.core.Flyway
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait

class LocalPostgresDatabase private constructor() : Database {

    private val memDataSource: HikariDataSource
    private val container = PostgreSQLContainer("postgres:14.5")

    companion object {
        private val instance by lazy {
            LocalPostgresDatabase().also {
                it.migrate()
            }
        }

        fun migratedDb(): LocalPostgresDatabase {
            instance.run {
                deleteWebTekst()
                deleteSmsTekst()
                deleteEpostTittel()
                deleteEpostTekst()
                deleteVarsel()
                deleteCoalescingRule()
                deleteCoalescingBackLog()
                deleteCoalescingHistoryWebTekst()
                deleteCoalescingHistorySmsTekst()
                deleteCoalescingHistoryEpostTittel()
                deleteCoalescingHistoryEpostTekst()
            }
            return instance
        }
    }

    init {
        container.waitingFor(Wait.forListeningPort())
        container.start()
        memDataSource = createDataSource()
    }

    override val dataSource: HikariDataSource
        get() = memDataSource

    private fun createDataSource(): HikariDataSource {
        return HikariDataSource().apply {
            jdbcUrl = container.jdbcUrl
            username = container.username
            password = container.password
            isAutoCommit = true
            maximumPoolSize = 3
            validate()
        }
    }

    private fun migrate() {

        createCloudSqlIAMUser()

        Flyway.configure()
            .connectRetries(3)
            .dataSource(dataSource)
            .load()
            .migrate()
    }

    // Workaround to fix migration involving missing user.
    private fun createCloudSqlIAMUser() = update {
        queryOf("create user cloudsqliamuser")
    }
}
