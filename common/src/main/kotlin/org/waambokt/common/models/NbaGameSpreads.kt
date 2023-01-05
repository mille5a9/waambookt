package org.waambokt.common.models

import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant

data class NbaGameSpreads(
    val gameId: Int, // espn game id
    val time: Instant,
    val homeTeamId: Int,
    val homeSpread: Double,
    val homePrice: Double,
    val homeBook: String,
    val awayTeamId: Int,
    val awaySpread: Double,
    val awayPrice: Double,
    val awayBook: String,
    val _id: Id<NbaGameSpreads> = newId()
)
