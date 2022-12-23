package org.waambokt.service.odds

import org.litote.kmongo.coroutine.CoroutineDatabase
import org.waambokt.service.odds.handlers.GetNbaOddsHandler
import org.waambokt.service.spec.odds.NbaOddsRequest
import org.waambokt.service.spec.odds.OddsServiceGrpcKt

class OddsService constructor(
    dbClient: CoroutineDatabase
) : OddsServiceGrpcKt.OddsServiceCoroutineImplBase() {
    private val getNbaOddsHandler = GetNbaOddsHandler(dbClient)
    override suspend fun getNbaOdds(request: NbaOddsRequest) = getNbaOddsHandler.handle(request)
}
