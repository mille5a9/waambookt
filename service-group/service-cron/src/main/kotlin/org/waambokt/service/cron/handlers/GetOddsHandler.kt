package org.waambokt.service.cron.handlers

import org.waambokt.service.spec.odds.NbaOddsRequest
import org.waambokt.service.spec.odds.OddsServiceGrpcKt

class GetOddsHandler(
    private val oddsService: OddsServiceGrpcKt.OddsServiceCoroutineStub
) {
    suspend fun handle() =
        oddsService.getNbaOdds(
            NbaOddsRequest.newBuilder()
                .addOddsMarkets(NbaOddsRequest.NbaOddsMarketsEnum.SPREADS)
                .build()
        )
}
