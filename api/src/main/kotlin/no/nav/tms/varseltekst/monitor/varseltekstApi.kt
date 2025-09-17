package no.nav.tms.varseltekst.monitor

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.tms.common.metrics.installTmsApiMetrics
import no.nav.tms.token.support.azure.validation.azure
import no.nav.tms.varseltekst.monitor.varseltekst.FileNotFoundException
import no.nav.tms.varseltekst.monitor.varseltekst.VarseltekstQueryService
import no.nav.tms.varseltekst.monitor.varseltekst.varseltekstRoutes
import java.io.File
import java.text.DateFormat

fun Application.varseltekstMonitor(
    queryHandler: VarseltekstQueryService,
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

                is FileNotFoundException -> {
                    call.respondText(
                        status = HttpStatusCode.NotFound,
                        text = "Fil med id [${cause.fileId}] finnes ikke, eller er allerede hentet"
                    )
                    log.warn(cause) { "Fil med id [${cause.fileId}] finnes ikke, eller er allerede hentet" }
                }

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
        varseltekstRoutes(queryHandler)
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
