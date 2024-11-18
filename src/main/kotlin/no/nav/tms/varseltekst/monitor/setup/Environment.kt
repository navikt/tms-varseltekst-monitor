package no.nav.tms.varseltekst.monitor.setup

import no.nav.tms.common.util.config.StringEnvVar.getEnvVar

data class Environment(val clusterName: String = getEnvVar("NAIS_CLUSTER_NAME"),
                       val namespace: String = getEnvVar("NAIS_NAMESPACE"),
                       val dbUser: String = getEnvVar("DB_USERNAME"),
                       val dbPassword: String = getEnvVar("DB_PASSWORD"),
                       val dbHost: String = getEnvVar("DB_HOST"),
                       val dbPort: String = getEnvVar("DB_PORT"),
                       val dbName: String = getEnvVar("DB_DATABASE"),
                       val dbUrl: String = getDbUrl(dbHost, dbPort, dbName),
                       val kafkaEnvironment: Map<String, String> = getKafkaEnv()
)

private fun getKafkaEnv(): Map<String, String> {
    return System.getenv().keepKeys(
        "KAFKA_BROKERS",
        "KAFKA_CONSUMER_GROUP_ID",
        "KAFKA_RAPID_TOPIC",
        "KAFKA_KEYSTORE_PATH",
        "KAFKA_CREDSTORE_PASSWORD",
        "KAFKA_TRUSTSTORE_PATH",
        "KAFKA_RESET_POLICY"
    ) + mapOf("HTTP_PORT" to "8080")
}

private fun Map<String, String>.keepKeys(vararg keys: String): Map<String, String> {
    val out = mutableMapOf<String, String>()

    keys.forEach { key ->
        if (contains(key)) {
            out[key] = get(key)!!
        }
    }

    return out
}

private fun getDbUrl(host: String, port: String, name: String): String {
    return if (host.endsWith(":$port")) {
        "jdbc:postgresql://${host}/$name"
    } else {
        "jdbc:postgresql://${host}:${port}/${name}"
    }
}
