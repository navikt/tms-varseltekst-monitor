import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    kotlin("jvm").version(Kotlin.version)
    kotlin("plugin.allopen").version(Kotlin.version)

    id(Flyway.pluginId) version (Flyway.version)
    id(Shadow.pluginId) version (Shadow.version)

    // Apply the application plugin to add support for building a CLI application.
    application
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
    maven("https://packages.confluent.io/maven")
    maven ( "https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven" )
    mavenLocal()
}

dependencies {
    implementation(DittNAV.Common.utils)
    implementation(Flyway.core)
    implementation(Hikari.cp)
    implementation(Influxdb.java)
    implementation(Ktor2.Server.netty)
    implementation(Ktor2.Server.htmlDsl)
    implementation(KotlinLogging.logging)
    implementation(Logstash.logbackEncoder)
    implementation(NAV.vaultJdbc)
    implementation(Postgresql.postgresql)
    implementation(Prometheus.common)
    implementation(Prometheus.hotspot)
    implementation(Prometheus.logback)
    implementation("com.github.navikt:rapids-and-rivers:2022100711511665136276.49acbaae4ed4")

    testImplementation(Junit.api)
    testImplementation(Junit.engine)
    testImplementation(Kafka.Apache.kafka_2_12)
    testImplementation(Kafka.Apache.streams)
    testImplementation(Kafka.Confluent.schemaRegistry)
    testImplementation(Mockk.mockk)
    testImplementation(NAV.kafkaEmbedded)
    testImplementation(TestContainers.postgresql)
    testImplementation(Kotest.runnerJunit5)
    testImplementation(Kotest.assertionsCore)
}

application {
    mainClass.set("no.nav.tms.varseltekst.monitor.config.ApplicationKt")

}

tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events("passed", "skipped", "failed")
        }
    }

    register("runServer", JavaExec::class) {
        println("Setting default environment variables for running with DittNAV docker-compose")
        DockerComposeDefaults.environomentVariables.forEach { (name, value) ->
            println("Setting the environment variable $name")
            environment(name, value)
        }

        main = application.mainClass.get()
        classpath = sourceSets["main"].runtimeClasspath
    }
}

// TODO: Fjern følgende work around i ny versjon av Shadow-pluginet:
// Skal være løst i denne: https://github.com/johnrengelman/shadow/pull/612
project.setProperty("mainClassName", application.mainClass.get())
apply(plugin = Shadow.pluginId)
