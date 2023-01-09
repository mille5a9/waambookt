package org.waambokt.common.models

import org.litote.kmongo.Id
import org.litote.kmongo.newId

data class NbaFourFactors(
    val gameId: Int,
    val teamId: Int,
    val shooting: Double,
    val turnovers: Double,
    val rebounds: Double,
    val freebies: Double,
    val _id: Id<NbaFourFactors> = newId()
)
