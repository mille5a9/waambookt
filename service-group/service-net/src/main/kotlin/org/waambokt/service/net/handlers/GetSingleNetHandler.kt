package org.waambokt.service.net.handlers

import org.litote.kmongo.coroutine.CoroutineDatabase
import org.waambokt.service.net.helpers.RefreshNetsHelper
import org.waambokt.service.net.helpers.RefreshNetsHelper.findTeamNet
import org.waambokt.service.spec.net.SingleNetRequest
import org.waambokt.service.spec.net.SingleNetResponse

class GetSingleNetHandler(
    private val db: CoroutineDatabase
) {
    suspend fun handle(request: SingleNetRequest): SingleNetResponse =
        SingleNetResponse.newBuilder()
            .setNetValue(RefreshNetsHelper.refreshNet(db).findTeamNet(request.teamName))
            .build()
}
