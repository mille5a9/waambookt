package org.waambokt.service.net

import org.waambokt.common.WaamboktGrpcServer
import org.waambokt.common.constants.Env
import org.waambokt.common.constants.Environment
import org.waambokt.service.odds.OddsService

fun main() {
    val env = Environment(Env.ODDS, Env.PORT, Env.MONGO_CONNECTION_STRING, Env.ISPROD, Env.GRPC)
    val port = env["PORT"].toInt()
    val server = WaamboktGrpcServer(
        port,
        NetService(env),
        OddsService(env)
    )
    server.start()
    server.blockUntilShutdown()
}
