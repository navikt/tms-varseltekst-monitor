package no.nav.tms.varseltekst.monitor

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import no.nav.tms.common.metrics.installTmsApiMetrics
import no.nav.tms.token.support.azure.validation.azure
import no.nav.tms.varseltekst.monitor.varseltekst.VarseltekstRepository
import no.nav.tms.varseltekst.monitor.varseltekst.varseltekstRoutes
import java.io.File
import java.text.DateFormat

fun Application.varseltekstMonitor(
    varseltekstRepository: VarseltekstRepository,
    installAuthenticatorsFunction: Application.() -> Unit = installAuth(),
) {

    val log = KotlinLogging.logger {}

    installAuthenticatorsFunction()

    installTmsApiMetrics {
        setupMetricsRoute = false
    }

    install(ContentNegotiation) {
        jackson {
            configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            registerModule(JavaTimeModule())
            dateFormat = DateFormat.getDateTimeInstance()
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {

                is IllegalArgumentException -> {
                    call.respondText(
                        status = HttpStatusCode.BadRequest,
                        text = cause.message ?: "Feil i parametre"
                    )
                    log.warn(cause) { "Feil i parametre" }
                }

                else -> {
                    call.respond(HttpStatusCode.InternalServerError)
                    log.warn(cause) { "Apikall feiler" }
                }
            }
        }
    }


    routing {
        varseltekstRoutes(varseltekstRepository)
        staticFiles("/", File("public")) {
            preCompressed(CompressedFileType.GZIP)
        }
    }
}

private fun installAuth(): Application.() -> Unit = {
    authentication {
        azure {
            setAsDefault = true
        }
    }
}
