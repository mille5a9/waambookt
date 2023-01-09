package org.waambokt.common.models

import org.litote.kmongo.Id
import org.litote.kmongo.newId

class NbaFeatures(
    val gameId: Int,
    val teamId: Int,
    val shooting: Double,
    val turnovers: Double,
    val rebounds: Double,
    val freebies: Double,
    val backToBack: Double,
    val bestSpread: Double,
    val result: Double,
    val _id: Id<NbaFeatures> = newId()
)
