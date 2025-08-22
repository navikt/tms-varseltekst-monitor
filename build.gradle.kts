import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm").version(Kotlin.version)

    id(TmsJarBundling.plugin)

    application
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
    mavenLocal()
}

dependencies {
    implementation(Flyway.core)
    implementation(Flyway.postgres)
    implementation(Hikari.cp)
    implementation(Ktor.Server.netty)
    implementation(Ktor.Server.contentNegotiation)
    implementation(Ktor.Server.htmlDsl)
    implementation(Ktor.Server.statusPages)
    implementation(Ktor.Serialization.jackson)
    implementation(KotlinLogging.logging)
    implementation(KotliQuery.kotliquery)
    implementation(Logstash.logbackEncoder)
    implementation(JacksonDatatype.datatypeJsr310)
    implementation(JacksonDatatype.moduleKotlin)
    implementation(Postgresql.postgresql)
    implementation(TmsKafkaTools.kafkaApplication)
    implementation(TmsCommonLib.utils)
    implementation(TmsCommonLib.metrics)

    testImplementation(JunitPlatform.launcher)
    testImplementation(JunitJupiter.api)
    testImplementation(JunitJupiter.params)
    testImplementation(Mockk.mockk)
    testImplementation(TestContainers.postgresql)
    testImplementation(Kotest.runnerJunit5)
    testImplementation(Kotest.assertionsCore)
}

application {
    mainClass.set("no.nav.tms.varseltekst.monitor.ApplicationKt")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events("passed", "skipped", "failed")
        }
    }
}
