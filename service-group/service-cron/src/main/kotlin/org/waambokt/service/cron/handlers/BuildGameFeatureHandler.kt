package org.waambokt.service.cron.handlers

import org.waambokt.service.spec.score.BuildGameFeaturesRequest
import org.waambokt.service.spec.score.ScoreServiceGrpcKt

class BuildGameFeatureHandler(
    private val scoreService: ScoreServiceGrpcKt.ScoreServiceCoroutineStub
) {
    suspend fun handle(gameId: Int) =
        scoreService.buildGameFeatures(
            BuildGameFeaturesRequest.newBuilder()
                .setGameId(gameId)
                .build()
        )
}
