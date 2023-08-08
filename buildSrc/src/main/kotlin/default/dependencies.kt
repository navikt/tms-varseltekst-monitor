package default

// Managed by tms-dependency-admin. Overrides and additions should be placed in separate file

interface DependencyGroup {
    val groupId: String? get() = null
    val version: String? get() = null

    fun dependency(name: String, groupId: String? = this.groupId, version: String? = this.version): String {
        requireNotNull(groupId)
        requireNotNull(version)

        return "$groupId:$name:$version"
    }
}

interface DittNAVCommonLibDefaults: DependencyGroup {
    override val version get() = "2022.09.30-12.41-aa46d2d75788"
    override val groupId get() = "com.github.navikt.dittnav-common-lib"

    val influxdb get() = dependency("dittnav-common-influxdb")
    val utils get() = dependency("dittnav-common-utils")
}

interface FlywayDefaults: DependencyGroup {
    override val groupId get() = "org.flywaydb"
    override val version get() = "9.21.1"

    val pluginId get() = "org.flywaydb.flyway"
    val core get() = dependency("flyway-core")
}

interface HikariDefaults: DependencyGroup {
    override val groupId get() = "com.zaxxer"
    override val version get() = "5.0.1"

    val cp get() = dependency("HikariCP")
}

interface JacksonDatatypeDefaults: DependencyGroup {
    override val version get() = "2.15.1"

    val datatypeJsr310 get() = dependency("jackson-datatype-jsr310", groupId = "com.fasterxml.jackson.datatype")
    val moduleKotlin get() = dependency("jackson-module-kotlin", groupId = "com.fasterxml.jackson.module")
}

interface JunitDefaults: DependencyGroup {
    override val groupId get() = "org.junit.jupiter"
    override val version get() = "5.10.0"

    val api get() = dependency("junit-jupiter-api")
    val engine get() = dependency("junit-jupiter-engine")
    val params get() = dependency("junit-jupiter-params")
}

interface JjwtDefaults: DependencyGroup {
    override val groupId get() = "io.jsonwebtoken"
    override val version get() = "0.11.5"

    val api get() = dependency("jjwt-api")
    val impl get() = dependency("jjwt-impl")
    val jackson get() = dependency("jjwt-jackson")
    val orgjson get() = dependency("jjwt-orgjson")
}

interface KafkaDefaults: DependencyGroup {
    override val groupId get() = "org.apache.kafka"
    override val version get() = "3.5.0"

    val clients get() = dependency("kafka-clients")
    val kafka_2_12 get() = dependency("kafka_2.12")
    val streams get() = dependency("kafka-streams")
}

interface KluentDefaults: DependencyGroup {
    override val groupId get() = "org.amshove.kluent"
    override val version get() = "1.72"

    val kluent get() = dependency("kluent")
}

interface KotestDefaults: DependencyGroup {
    override val groupId get() = "io.kotest"
    override val version get() = "5.6.1"

    val runnerJunit5 get() = dependency("kotest-runner-junit5")
    val assertionsCore get() = dependency("kotest-assertions-core")
    val extensions get() = dependency("kotest-extensions")
}

interface KotlinDefaults: DependencyGroup {
    override val groupId get() = "org.jetbrains.kotlin"
    override val version get() = "1.9.0"

    val reflect get() = dependency("kotlin-reflect")
}

interface KotlinLoggingDefaults: DependencyGroup {
    override val groupId get() = "io.github.oshai"
    override val version get() = "5.0.2"

    val logging get() = dependency("kotlin-logging")
}

interface KotlinxDefaults: DependencyGroup {
    override val groupId get() = "org.jetbrains.kotlinx"

    val coroutines get() = dependency("kotlinx-coroutines-core", version = "1.7.3")
}

interface KotliQueryDefaults: DependencyGroup {
    override val groupId get() = "com.github.seratch"
    override val version get() = "1.9.0"

    val kotliquery get() = dependency("kotliquery")
}

object KtorDefaults {
    val version get() = "2.3.2"
    val groupId get() = "io.ktor"

    interface ServerDefaults: DependencyGroup {
        override val groupId get() = KtorDefaults.groupId
        override val version get() = KtorDefaults.version

        val core get() = dependency("ktor-server-core")
        val netty get() = dependency("ktor-server-netty")
        val defaultHeaders get() = dependency("ktor-server-default-headers")
        val metricsMicrometer get() = dependency("ktor-server-metrics-micrometer")
        val auth get() = dependency("ktor-server-auth")
        val authJwt get() = dependency("ktor-server-auth-jwt")
        val contentNegotiation get() = dependency("ktor-server-content-negotiation")
        val statusPages get() = dependency("ktor-server-status-pages")
        val htmlDsl get() = dependency("ktor-server-html-builder")
        val cors get() = dependency("ktor-server-cors")
    }

    interface ClientDefaults: DependencyGroup {
        override val groupId get() = KtorDefaults.groupId
        override val version get() = KtorDefaults.version

        val core get() = dependency("ktor-client-core")
        val apache get() = dependency("ktor-client-apache")
        val contentNegotiation get() = dependency("ktor-client-content-negotiation")
    }

    interface SerializationDefaults: DependencyGroup {
        override val groupId get() = KtorDefaults.groupId
        override val version get() = KtorDefaults.version

        val kotlinX get() = dependency("ktor-serialization-kotlinx-json")
        val jackson get() = dependency("ktor-serialization-jackson")
    }

    interface TestDefaults: DependencyGroup {
        override val groupId get() = KtorDefaults.groupId
        override val version get() = KtorDefaults.version

        val clientMock get() = dependency("ktor-client-mock")
        val serverTestHost get() = dependency("ktor-server-test-host")
    }
}

interface LogstashDefaults: DependencyGroup {
    override val groupId get() = "net.logstash.logback"
    override val version get() = "7.4"

    val logbackEncoder get() = dependency("logstash-logback-encoder")
}

interface MicrometerDefaults: DependencyGroup {
    override val groupId get() = "io.micrometer"
    override val version get() = "1.11.2"

    val registryPrometheus get() = dependency("micrometer-registry-prometheus")
}

interface MockkDefaults: DependencyGroup {
    override val groupId get() = "io.mockk"
    override val version get() = "1.13.5"

    val mockk get() = dependency("mockk")
}

interface PostgresqlDefaults: DependencyGroup {
    override val groupId get() = "org.postgresql"
    override val version get() = "42.6.0"

    val postgresql get() = dependency("postgresql")
}

interface PrometheusDefaults: DependencyGroup {
    override val version get() = "0.16.0"
    override val groupId get() = "io.prometheus"

    val common get() = dependency("simpleclient_common")
    val hotspot get() = dependency("simpleclient_hotspot")
    val httpServer get() = dependency("simpleclient_httpserver")
    val logback get() = dependency("simpleclient_logback")
    val simpleClient get() = dependency("simpleclient")
}

interface RapidsAndRiversDefaults: DependencyGroup {
    override val groupId get() = "com.github.navikt"
    override val version get() = "2023080113411690890096.310ed8e5ed93"

    val rapidsAndRivers get() = dependency("rapids-and-rivers")
}

interface ShadowDefaults: DependencyGroup {
    override val version get() = "7.1.2"

    val pluginId get() = "com.github.johnrengelman.shadow"
}

interface TestContainersDefaults: DependencyGroup {
    override val version get() = "1.18.3"
    override val groupId get() = "org.testcontainers"

    val junitJupiter get() = dependency("junit-jupiter")
    val testContainers get() = dependency("testcontainers")
    val postgresql get() = dependency("postgresql")
}

interface TmsCommonLibDefaults: DependencyGroup {
    override val groupId get() = "com.github.navikt"
    override val version get() = "1.5.0"

    val commonLib get() = dependency("tms-common-lib")
}

interface TmsKtorTokenSupportDefaults: DependencyGroup {
    override val groupId get() = "com.github.navikt.tms-ktor-token-support"
    override val version get() = "2.2.0"

    val authenticationInstaller get() = dependency("token-support-authentication-installer")
    val azureExchange get() = dependency("token-support-azure-exchange")
    val azureValidation get() = dependency("token-support-azure-validation")
    val tokenXValidation get() = dependency("token-support-tokenx-validation")
    val authenticationInstallerMock get() = dependency("token-support-authentication-installer-mock")
    val tokenXValidationMock get() = dependency("token-support-tokenx-validation-mock")
    val azureValidationMock get() = dependency("token-support-azure-validation-mock")
    val tokendingsExchange get() = dependency("token-support-tokendings-exchange")
    val idportenSidecar get() = dependency("token-support-idporten-sidecar")
    val idportenSidecarMock get() = dependency("token-support-idporten-sidecar-mock")
}
