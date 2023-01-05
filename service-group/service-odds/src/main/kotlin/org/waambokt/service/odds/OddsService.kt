package org.waambokt.service.odds

import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.waambokt.common.constants.Environment
import org.waambokt.common.extensions.EnvironmentExtension.bool
import org.waambokt.service.odds.handlers.GetNbaOddsHandler
import org.waambokt.service.spec.odds.NbaOddsRequest
import org.waambokt.service.spec.odds.OddsServiceGrpcKt

class OddsService constructor(
    env: Environment
) : OddsServiceGrpcKt.OddsServiceCoroutineImplBase() {
    private val dbClient = KMongo.createClient(env["MONGO_CONNECTION_STRING"])
        .getDatabase(if (env.bool("ISPROD")) "prodkt" else "testkt")
        .coroutine

    private val getNbaOddsHandler = GetNbaOddsHandler(env, dbClient)
    override suspend fun getNbaOdds(request: NbaOddsRequest) = getNbaOddsHandler.handle(request)
}
