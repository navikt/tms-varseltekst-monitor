package no.nav.tms.varseltekst.monitor.setup

import no.nav.tms.common.util.config.StringEnvVar.getEnvVar

data class Environment(val clusterName: String = getEnvVar("NAIS_CLUSTER_NAME"),
                       val namespace: String = getEnvVar("NAIS_NAMESPACE"),
                       val dbUser: String = getEnvVar("DB_USERNAME"),
                       val dbPassword: String = getEnvVar("DB_PASSWORD"),
                       val dbUrl: String = getDbUrl(),
                       val varselTopic: String = getEnvVar("VARSEL_TOPIC"),
                       val groupId: String = getEnvVar("KAFKA_GROUP_ID")
)

private fun getDbUrl(): String {
    val host: String = getEnvVar("DB_HOST")
    val port: String = getEnvVar("DB_PORT")
    val name: String = getEnvVar("DB_DATABASE")

    return if (host.endsWith(":$port")) {
        "jdbc:postgresql://${host}/$name"
    } else {
        "jdbc:postgresql://${host}:${port}/${name}"
    }
}
