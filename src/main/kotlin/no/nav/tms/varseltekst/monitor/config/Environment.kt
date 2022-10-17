package no.nav.tms.varseltekst.monitor.config

import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVar

data class Environment(val clusterName: String = getEnvVar("NAIS_CLUSTER_NAME"),
                       val namespace: String = getEnvVar("NAIS_NAMESPACE"),
                       val dbUser: String = getEnvVar("DB_USERNAME"),
                       val dbPassword: String = getEnvVar("DB_PASSWORD"),
                       val dbHost: String = getEnvVar("DB_HOST"),
                       val dbPort: String = getEnvVar("DB_PORT"),
                       val dbName: String = getEnvVar("DB_DATABASE"),
                       val dbUrl: String = getDbUrl(dbHost, dbPort, dbName)
)

fun getDbUrl(host: String, port: String, name: String): String {
    return if (host.endsWith(":$port")) {
        "jdbc:postgresql://${host}/$name"
    } else {
        "jdbc:postgresql://${host}:${port}/${name}"
    }
}
