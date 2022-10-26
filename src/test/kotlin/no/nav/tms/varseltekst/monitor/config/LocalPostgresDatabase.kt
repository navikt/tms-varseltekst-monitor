package no.nav.tms.varseltekst.monitor.config

import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.tms.varseltekst.monitor.database.Database
import org.flywaydb.core.Flyway
import org.slf4j.Logger
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.containers.wait.strategy.WaitAllStrategy

class LocalPostgresDatabase private constructor() : Database {

    override val log = KotlinLogging.logger {}

    private val memDataSource: HikariDataSource
    private val container = PostgreSQLContainer("postgres:14.5")

    companion object {
        private val instance by lazy {
            LocalPostgresDatabase().also {
                it.migrate()
            }
        }

        fun migratedDb(): LocalPostgresDatabase {
            instance.dbQuery {
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
            isAutoCommit = false
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
    private fun createCloudSqlIAMUser() {
        dbQuery {
            prepareStatement("create user cloudsqliamuser").executeUpdate()
        }
    }
}
