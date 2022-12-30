package org.waambokt.service.net

import io.grpc.ManagedChannelBuilder
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo.createClient
import org.waambokt.common.WaamboktGrpcServer
import org.waambokt.common.constants.Env
import org.waambokt.common.constants.Environment
import org.waambokt.common.extensions.EnvironmentExtension.bool
import org.waambokt.service.odds.OddsService
import org.waambokt.service.spec.odds.OddsServiceGrpcKt

fun main() {
    val env = Environment(Env.ODDS, Env.PORT, Env.MONGO_CONNECTION_STRING, Env.ISPROD, Env.GRPC)
    val port = env["PORT"].toInt()
    val server = WaamboktGrpcServer(
        port,
        NetService(
            createClient(env["MONGO_CONNECTION_STRING"])
                .getDatabase(if (env.bool("ISPROD")) "prodkt" else "testkt")
                .coroutine,
            OddsServiceGrpcKt.OddsServiceCoroutineStub(
                ManagedChannelBuilder.forAddress(env["GRPC"], port).usePlaintext().build()
            )
        ),
        OddsService(Environment(Env.PORT, Env.ODDS))
    )
    server.start()
    server.blockUntilShutdown()
}
