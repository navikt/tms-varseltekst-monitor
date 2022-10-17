package no.nav.tms.varseltekst.monitor.metrics

import io.prometheus.client.Counter
import io.prometheus.client.Gauge

object PrometheusMetricsCollector {

    const val NAMESPACE = "dittnav_consumer"

    const val EVENTS_SEEN_NAME = "kafka_events_seen"
    const val EVENTS_PROCESSED_NAME = "kafka_events_processed"
    const val EVENTS_FAILED_NAME = "kafka_events_failed"
    const val EVENTS_DUPLICATE_KEY_NAME = "kafka_events_duplicate_key"
    const val EVENT_LAST_SEEN_NAME = "kafka_event_type_last_seen"
    const val KAFKA_TOPIC_TOTAL_EVENTS = "kafka_topic_total_events"
    const val KAFKA_TOPIC_TOTAL_EVENTS_BY_PRODUCER = "kafka_topic_total_events_by_producer"
    const val KAFKA_TOPIC_UNIQUE_EVENTS = "kafka_topic_unique_events"
    const val KAFKA_TOPIC_UNIQUE_EVENTS_BY_PRODUCER = "kafka_topic_unique_events_by_producer"
    const val KAFKA_TOPIC_DUPLICATED_EVENTS = "kafka_topic_duplicated_events"
    const val DB_CACHED_EVENTS = "db_cached_events"
    const val DB_TOTAL_EVENTS = "db_total_events"
    const val DB_TOTAL_EVENTS_BY_PRODUCER = "db_total_events_by_producer"

    private val MESSAGES_SEEN: Counter = Counter.build()
            .name(EVENTS_SEEN_NAME)
            .namespace(NAMESPACE)
            .help("Events read since last startup")
            .labelNames("type", "producer")
            .register()

    private val MESSAGES_PROCESSED: Counter = Counter.build()
            .name(EVENTS_PROCESSED_NAME)
            .namespace(NAMESPACE)
            .help("Events successfully processed since last startup")
            .labelNames("type", "producer")
            .register()

    private val MESSAGES_FAILED: Counter = Counter.build()
            .name(EVENTS_FAILED_NAME)
            .namespace(NAMESPACE)
            .help("Events failed since last startup")
            .labelNames("type", "producer")
            .register()

    private val MESSAGES_DUPLICATE_KEY: Counter = Counter.build()
            .name(EVENTS_DUPLICATE_KEY_NAME)
            .namespace(NAMESPACE)
            .help("Events skipped due to duplicate keys since last startup")
            .labelNames("type", "producer")
            .register()

    private val MESSAGE_LAST_SEEN: Gauge = Gauge.build()
            .name(EVENT_LAST_SEEN_NAME)
            .namespace(NAMESPACE)
            .help("Last time event type was seen")
            .labelNames("type", "producer")
            .register()

    private val MESSAGES_CACHED: Counter = Counter.build()
            .name(DB_CACHED_EVENTS)
            .namespace(NAMESPACE)
            .help("Number of events of type in DB-table")
            .labelNames("type", "producer")
            .register()

    private val MESSAGES_UNIQUE: Gauge = Gauge.build()
            .name(KAFKA_TOPIC_UNIQUE_EVENTS)
            .namespace(NAMESPACE)
            .help("Number of unique events of type on topic")
            .labelNames("type")
            .register()

    private val MESSAGES_UNIQUE_BY_PRODUCER: Gauge = Gauge.build()
            .name(KAFKA_TOPIC_UNIQUE_EVENTS_BY_PRODUCER)
            .namespace(NAMESPACE)
            .help("Number of unique events of type on topic grouped by producer")
            .labelNames("type", "producer")
            .register()

    private val MESSAGES_DUPLICATES: Gauge = Gauge.build()
            .name(KAFKA_TOPIC_DUPLICATED_EVENTS)
            .namespace(NAMESPACE)
            .help("Number of duplicated events of type on topic")
            .labelNames("type", "producer")
            .register()

    private val MESSAGES_TOTAL: Gauge = Gauge.build()
            .name(KAFKA_TOPIC_TOTAL_EVENTS)
            .namespace(NAMESPACE)
            .help("Total number of events of type on topic")
            .labelNames("type")
            .register()

    private val MESSAGES_TOTAL_BY_PRODUCER: Gauge = Gauge.build()
            .name(KAFKA_TOPIC_TOTAL_EVENTS_BY_PRODUCER)
            .namespace(NAMESPACE)
            .help("Total number of events of type on topic grouped by producer")
            .labelNames("type", "producer")
            .register()

    private val CACHED_EVENTS_IN_TOTAL: Gauge = Gauge.build()
            .name(DB_TOTAL_EVENTS)
            .namespace(NAMESPACE)
            .help("Total number of events of type in the cache")
            .labelNames("type")
            .register()

    private val CACHED_EVENTS_IN_TOTAL_BY_PRODUCER: Gauge = Gauge.build()
            .name(DB_TOTAL_EVENTS_BY_PRODUCER)
            .namespace(NAMESPACE)
            .help("Total number of events of type in the cache grouped by producer")
            .labelNames("type", "producer")
            .register()

    fun registerEventsSeen(count: Int, eventType: String, producer: String) {
        MESSAGES_SEEN.labels(eventType, producer).inc(count.toDouble())
        MESSAGE_LAST_SEEN.labels(eventType, producer).setToCurrentTime()
    }

    fun registerEventsProcessed(count: Int, topic: String, producer: String) {
        MESSAGES_PROCESSED.labels(topic, producer).inc(count.toDouble())
    }

    fun registerEventsFailed(count: Int, topic: String, producer: String) {
        MESSAGES_FAILED.labels(topic, producer).inc(count.toDouble())
    }

    fun registerEventsDuplicateKey(count: Int, topic: String, producer: String) {
        MESSAGES_DUPLICATE_KEY.labels(topic, producer).inc(count.toDouble())
    }

}
