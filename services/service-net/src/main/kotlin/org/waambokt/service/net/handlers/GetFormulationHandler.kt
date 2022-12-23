package org.waambokt.service.net.handlers

import org.litote.kmongo.coroutine.CoroutineDatabase
import org.waambokt.service.spec.net.FormulaRequest
import org.waambokt.service.spec.net.FormulaResponse

class GetFormulationHandler constructor(
    db: CoroutineDatabase
) {
    suspend fun handle(request: FormulaRequest): FormulaResponse {
        return FormulaResponse.getDefaultInstance()
    }
}
