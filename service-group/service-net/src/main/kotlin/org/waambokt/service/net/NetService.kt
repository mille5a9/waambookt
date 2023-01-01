package org.waambokt.service.net

import io.grpc.ManagedChannelBuilder
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo.createClient
import org.waambokt.common.constants.Environment
import org.waambokt.common.extensions.EnvironmentExtension.bool
import org.waambokt.service.net.handlers.GetFormulationHandler
import org.waambokt.service.spec.net.FormulaRequest
import org.waambokt.service.spec.net.NetServiceGrpcKt
import org.waambokt.service.spec.odds.OddsServiceGrpcKt

class NetService constructor(
    env: Environment
) : NetServiceGrpcKt.NetServiceCoroutineImplBase() {
    private val oddsService = OddsServiceGrpcKt.OddsServiceCoroutineStub(
        ManagedChannelBuilder.forAddress(env["GRPC"], env["PORT"].toInt()).usePlaintext().build()
    )
    private val dbClient = createClient(env["MONGO_CONNECTION_STRING"])
        .getDatabase(if (env.bool("ISPROD")) "prodkt" else "testkt")
        .coroutine
    private val getFormulationHandler = GetFormulationHandler(dbClient, oddsService)
    override suspend fun getFormulation(request: FormulaRequest) = getFormulationHandler.handle(request)
}
