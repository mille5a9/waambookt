package org.waambokt.service.net

import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo.createClient
import org.waambokt.common.WaamboktGrpcServer
import org.waambokt.common.constants.Env
import org.waambokt.common.constants.Environment
import org.waambokt.common.extensions.EnvironmentExtension.bool

fun main() {
    val env = Environment(Env.PORT, Env.MONGO_CONNECTION_STRING, Env.ISPROD)
    val port = env["PORT"].toInt()
    val server = WaamboktGrpcServer(
        port,
        NetService(
            createClient(env["MONGO_CONNECTION_STRING"])
                .getDatabase(if (env.bool("ISPROD")) "prodkt" else "testkt")
                .coroutine
        )
    )
    server.start()
    server.blockUntilShutdown()
}
