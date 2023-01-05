package org.waambokt.service.cron.handlers

import com.google.protobuf.Timestamp
import org.waambokt.service.spec.score.ScoreServiceGrpcKt
import org.waambokt.service.spec.score.StoreDayGamesRequest
import org.waambokt.service.spec.score.StoreDayGamesResponse
import java.time.Instant

class SaveScoreHandler(
    private val scoreService: ScoreServiceGrpcKt.ScoreServiceCoroutineStub
) {
    suspend fun handle(): StoreDayGamesResponse {
        return scoreService.storeDayGames(
            StoreDayGamesRequest.newBuilder()
                .setDay(
                    Timestamp.newBuilder()
                        .setSeconds(Instant.now().minusSeconds(secondsInDay).epochSecond)
                        .build()
                )
                .build()
        )
    }

    companion object {
        private const val secondsInDay = 86400L
    }
}
