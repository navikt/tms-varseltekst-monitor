package no.nav.tms.varseltekst.monitor.util

import kotlinx.coroutines.*
import kotlin.time.Duration

abstract class PeriodicJob(private val interval: Duration) {

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    abstract val job: Job

    protected fun initializeJob(periodicProcess: suspend () -> Unit) = scope.launch(start = CoroutineStart.LAZY) {
        while (job.isActive) {
            periodicProcess()
            delay(interval.inWholeMilliseconds)
        }
    }

    private fun className() = this::class.simpleName!!

    fun start() {
        job.start()
    }

    fun isCompleted(): Boolean {
        return job.isCompleted
    }

    suspend fun stop() {
        job.cancelAndJoin()
    }
}
