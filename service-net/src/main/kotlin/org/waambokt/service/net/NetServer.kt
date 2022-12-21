package org.waambokt.service.net

import io.grpc.Server
import io.grpc.ServerBuilder
import org.waambokt.service.spec.net.FormulaRequest
import org.waambokt.service.spec.net.FormulaResponse
import org.waambokt.service.spec.net.NetServiceGrpcKt

class NetServer(
    private val port: Int
) {
    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(NetService())
        .build()

    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@NetServer.stop()
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

    private class NetService : NetServiceGrpcKt.NetServiceCoroutineImplBase() {
        override suspend fun getFormulation(request: FormulaRequest): FormulaResponse {
            return FormulaResponse.newBuilder().defaultInstanceForType
        }
    }
}
