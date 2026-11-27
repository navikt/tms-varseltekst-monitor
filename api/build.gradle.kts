import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    alias(managed.plugins.kotlin)
    alias(managed.plugins.tms.jarBundling)
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
    implementation(libs.apache.poi)
    implementation(libs.apache.ooxml)

    implementation(managed.flyway.core)
    implementation(managed.hikariCp)
    implementation(managed.kotlinx.coroutines)
    implementation(managed.ktor.server.contentNegotiation)
    implementation(managed.ktor.server.auth)
    implementation(managed.ktor.serialization.jackson)
    implementation(managed.kotlinLogging)
    implementation(managed.kotliQuery)
    implementation(managed.jackson.datatypeJsr310)
    implementation(managed.jackson.moduleKotlin)
    implementation(managed.tms.commonLib.utils)
    implementation(managed.tms.commonLib.metrics)
    implementation(managed.tms.commonLib.postgres)
    implementation(managed.tms.commonLib.teamLogger)
    implementation(managed.tms.kafka.application)
    implementation(managed.tms.ktorTokenSupport.entraId.verification)
    implementation(managed.ktor.server.netty)
    implementation(managed.ktor.server.statusPages)

    runtimeOnly(managed.flyway.postgres)
    runtimeOnly(managed.logback.encoder)
    runtimeOnly(managed.postgresql)

    testImplementation(managed.junit.api)
    testImplementation(managed.junit.params)
    testImplementation(managed.testContainers.postgresql)
    testImplementation(managed.kotest.assertion)

    testRuntimeOnly(managed.junit.launcher)
    testRuntimeOnly(managed.junit.engine)
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
