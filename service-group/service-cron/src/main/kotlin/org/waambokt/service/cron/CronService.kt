package org.waambokt.service.cron

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.waambokt.common.constants.Environment
import org.waambokt.service.cron.handlers.SaveScoreHandler
import org.waambokt.service.spec.score.ScoreServiceGrpcKt
import java.time.LocalDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class CronService(
    env: Environment
) : CoroutineScope {
    private var job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val scoreService = ScoreServiceGrpcKt.ScoreServiceCoroutineStub(
        ManagedChannelBuilder.forAddress(env["GRPC"], env["PORT"].toInt()).usePlaintext().build()
    )

    fun stop() = job.cancel()

    suspend fun join() = job.join()

    fun start(
        delaySeconds: Int
    ) = launch {
        val saveScoreHandler = SaveScoreHandler(scoreService)
        var daily = 0
        while (true) {
            logger.info { "...$delaySeconds seconds elapsed (CronService health check)" }
            delay(delaySeconds.toDuration(DurationUnit.SECONDS))
            val dateTimeInfo = LocalDateTime.now()

            // check things that need to be checked every x seconds
            // first: trigger daily at 9 am
            if (dateTimeInfo.hour > 8 && dateTimeInfo.dayOfMonth != daily) {
                daily = dateTimeInfo.dayOfMonth
                val saveScoreResponse = saveScoreHandler.handle()
                logger.info { "saveScoreResponse: $saveScoreResponse" }
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
