package org.waambokt.service.score

import io.grpc.ManagedChannelBuilder
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.waambokt.common.constants.Environment
import org.waambokt.common.extensions.EnvironmentExtension.bool
import org.waambokt.service.score.handlers.StoreDayGamesHandler
import org.waambokt.service.spec.net.NetServiceGrpcKt
import org.waambokt.service.spec.score.ScoreServiceGrpcKt
import org.waambokt.service.spec.score.StoreDayGamesRequest
import org.waambokt.service.spec.score.StoreDayGamesResponse

class ScoreService(
    env: Environment
) : ScoreServiceGrpcKt.ScoreServiceCoroutineImplBase() {
    private val dbClient = KMongo.createClient(env["MONGO_CONNECTION_STRING"])
        .getDatabase(if (env.bool("ISPROD")) "prodkt" else "testkt")
        .coroutine

    private val netService = NetServiceGrpcKt.NetServiceCoroutineStub(
        ManagedChannelBuilder.forAddress(env["GRPC"], env["PORT"].toInt()).usePlaintext().build()
    )
    private val storeDayGamesHandler = StoreDayGamesHandler(dbClient, netService)

    override suspend fun storeDayGames(request: StoreDayGamesRequest): StoreDayGamesResponse {
        return storeDayGamesHandler.handle(request)
    }
}
