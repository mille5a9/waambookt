package org.waambokt.service.net

import org.litote.kmongo.coroutine.CoroutineDatabase
import org.waambokt.service.net.handlers.GetFormulationHandler
import org.waambokt.service.spec.net.FormulaRequest
import org.waambokt.service.spec.net.NetServiceGrpcKt

class NetService constructor(
    dbClient: CoroutineDatabase
) : NetServiceGrpcKt.NetServiceCoroutineImplBase() {
    private val getFormulationHandler = GetFormulationHandler(dbClient)
    override suspend fun getFormulation(request: FormulaRequest) = getFormulationHandler.handle(request)
}
