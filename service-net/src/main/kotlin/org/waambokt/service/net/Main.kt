package org.waambokt.service.net

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 50051
    val server = NetServer(port)
    server.start()
    server.blockUntilShutdown()
}
