package org.waambokt.service.odds

import org.waambokt.common.WaamboktGrpcServer
import org.waambokt.common.constants.Env
import org.waambokt.common.constants.Environment

fun main() {
    val env = Environment(Env.PORT, Env.ODDS)
    val port = env["PORT"].toInt()
    val server = WaamboktGrpcServer(
        port,
        OddsService(env)
    )
    server.start()
    server.blockUntilShutdown()
}
