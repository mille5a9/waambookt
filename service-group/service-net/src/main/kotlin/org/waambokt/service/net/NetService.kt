package org.waambokt.service.net

import org.litote.kmongo.coroutine.CoroutineDatabase
import org.waambokt.service.net.handlers.GetFormulationHandler
import org.waambokt.service.spec.net.FormulaRequest
import org.waambokt.service.spec.net.NetServiceGrpcKt
import org.waambokt.service.spec.odds.OddsServiceGrpcKt

class NetService constructor(
    dbClient: CoroutineDatabase,
    oddsService: OddsServiceGrpcKt.OddsServiceCoroutineStub
) : NetServiceGrpcKt.NetServiceCoroutineImplBase() {
    private val getFormulationHandler = GetFormulationHandler(dbClient, oddsService)
    override suspend fun getFormulation(request: FormulaRequest) = getFormulationHandler.handle(request)
}
