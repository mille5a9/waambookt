package org.waambokt.common.models

import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant

data class NbaBox(
    val gameId: Int,
    val time: Instant,
    val home: BoxTeam,
    val away: BoxTeam,
    val _id: Id<NbaBox> = newId()
) {
    data class BoxTeam(
        val teamId: Int,
        val fg: Int,
        val fg3p: Int,
        val fga: Int,
        val turnovers: Int,
        val ft: Int,
        val fta: Int,
        val oreb: Int,
        val dreb: Int,
        val adj_net: Double,
        val score: Int
    )
}
