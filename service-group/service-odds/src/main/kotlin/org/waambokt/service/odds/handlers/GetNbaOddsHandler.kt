package org.waambokt.service.odds.handlers

import org.litote.kmongo.coroutine.CoroutineDatabase
import org.waambokt.service.spec.odds.NbaOddsRequest
import org.waambokt.service.spec.odds.NbaOddsResponse

class GetNbaOddsHandler constructor(
    db: CoroutineDatabase
) {
    suspend fun handle(request: NbaOddsRequest): NbaOddsResponse {
        return NbaOddsResponse.getDefaultInstance()
    }
}
