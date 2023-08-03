import default.*

// Managed by tms-dependency-admin. Overrides and additions should be placed in separate file

object DittNAVCommonLib: DittNAVCommonLibDefaults
object Flyway: FlywayDefaults
object Hikari: HikariDefaults
object JacksonDatatype: JacksonDatatypeDefaults
object Junit: JunitDefaults
object Jjwt: JjwtDefaults
object Kafka: KafkaDefaults
object Kluent: KluentDefaults
object Kotest: KotestDefaults
object Kotlin: KotlinDefaults
object KotlinLogging: KotlinLoggingDefaults
object Kotlinx: KotlinxDefaults
object KotliQuery: KotliQueryDefaults
object Ktor {
    object Server: KtorDefaults.ServerDefaults
    object Client: KtorDefaults.ClientDefaults
    object Serialization: KtorDefaults.SerializationDefaults
    object Test: KtorDefaults.TestDefaults
}
object Logstash: LogstashDefaults
object Micrometer: MicrometerDefaults
object Mockk: MockkDefaults
object Postgresql: PostgresqlDefaults
object Prometheus: PrometheusDefaults
object RapidsAndRivers: RapidsAndRiversDefaults
object Shadow: ShadowDefaults
object TestContainers: TestContainersDefaults
object TmsKtorTokenSupport: TmsKtorTokenSupportDefaults
