package no.nav.tms.varseltekst.monitor.setup

import no.nav.tms.common.util.config.StringEnvVar.getEnvVar

data class Environment(
    val jdbcUrl: String = jdbcUrl(),
    val clusterName: String = getEnvVar("NAIS_CLUSTER_NAME"),
    val namespace: String = getEnvVar("NAIS_NAMESPACE"),
    val varselTopic: String = getEnvVar("VARSEL_TOPIC"),
    val groupId: String = getEnvVar("KAFKA_GROUP_ID")
)

private fun jdbcUrl(): String {
    val host: String = getEnvVar("DB_HOST")
    val name: String = getEnvVar("DB_DATABASE")
    val user: String = getEnvVar("DB_USERNAME")
    val password: String = getEnvVar("DB_PASSWORD")

    return "jdbc:postgresql://${host}/$name?user=$user&password=$password"
}

