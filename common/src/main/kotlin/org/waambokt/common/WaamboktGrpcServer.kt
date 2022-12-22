package org.waambokt.common

import io.grpc.BindableService
import io.grpc.Server
import io.grpc.ServerBuilder

class WaamboktGrpcServer(
    private val port: Int,
    service: BindableService
) {
    private val server: Server = ServerBuilder.forPort(port)
        .addService(service)
        .build()

    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@WaamboktGrpcServer.stop()
                println("*** server shut down")
            }
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}
