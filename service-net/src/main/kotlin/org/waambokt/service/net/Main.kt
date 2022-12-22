package org.waambokt.service.net

import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo.createClient
import org.waambokt.common.WaamboktGrpcServer
import org.waambokt.common.constants.Env
import org.waambokt.common.constants.Environment

fun main() {
    val env = Environment(Env.PORT, Env.MONGO_CONNECTION_STRING, Env.PROD)
    val port = env["PORT"]?.toInt() ?: 50051
    val server = WaamboktGrpcServer(
        port,
        NetService(
            createClient(env["MONGO_CONNECTION_STRING"]!!)
                .getDatabase(env["PROD", "prodkt", "testkt"])
                .coroutine
        )
    )
    server.start()
    server.blockUntilShutdown()
}
