package org.waambokt.service.cron

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.waambokt.common.constants.Environment
import org.waambokt.service.cron.handlers.BuildGameFeatureHandler
import org.waambokt.service.cron.handlers.GetOddsHandler
import org.waambokt.service.cron.handlers.SaveScoreHandler
import org.waambokt.service.spec.odds.OddsServiceGrpcKt
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

    private val oddsService = OddsServiceGrpcKt.OddsServiceCoroutineStub(
        ManagedChannelBuilder.forAddress(env["GRPC"], env["PORT"].toInt()).usePlaintext().build()
    )

    fun stop() = job.cancel()

    suspend fun join() = job.join()

    fun start(
        delaySeconds: Int
    ) = launch {
        val saveScoreHandler = SaveScoreHandler(scoreService)
        val getOddsHandler = GetOddsHandler(oddsService)
        val buildGameFeatureHandler = BuildGameFeatureHandler(scoreService)
        var dailyScoreLoadDay = 0
        var dailyOddsLoadDay = 0
        while (true) {
            logger.info { "...$delaySeconds seconds elapsed (CronService health check)" }
            delay(delaySeconds.toDuration(DurationUnit.SECONDS))
            val dateTimeInfo = LocalDateTime.now()

            // check things that need to be checked every x seconds
            // first: trigger daily at 9 am
            if (dateTimeInfo.hour > 8 && dateTimeInfo.dayOfMonth != dailyScoreLoadDay) {
                dailyScoreLoadDay = dateTimeInfo.dayOfMonth
                // load yesterday's final scores
                val saveScoreResponse = saveScoreHandler.handle()
                logger.info { "saveScoreResponse: $saveScoreResponse" }

                // curate NN feature data using the newly acquired game results & odds from yesterday
                val buildGameFeatureResponses = saveScoreResponse.gameResultList.map {
                    buildGameFeatureHandler.handle(it.gameId)
                }
                logger.info { "buildGameFeatureResponses: $buildGameFeatureResponses" }
            }

            // trigger daily at 1 pm
            if (dateTimeInfo.hour > 12 && dateTimeInfo.dayOfMonth != dailyOddsLoadDay) {
                dailyOddsLoadDay = dateTimeInfo.dayOfMonth
                val nbaOddsResponse = getOddsHandler.handle()
                logger.info { "nbaOddsResponse: $nbaOddsResponse" }
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
