package org.waambokt.service.cron

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class CronService : CoroutineScope {
    private var job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    fun stop() = job.cancel()

    suspend fun join() = job.join()

    fun start(delaySeconds: Int) = launch {
        while (true) {
            logger.info { "...task tick (health check).." }
            delay(delaySeconds.toDuration(DurationUnit.SECONDS))

            // check things that need to be checked every x seconds
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
