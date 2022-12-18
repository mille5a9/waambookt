package waambokt.models

import org.litote.kmongo.Id
import org.litote.kmongo.newId

data class NbaMatchup(
    val gameId: String,
    val homeName: String,
    val awayName: String,
    val homeSpread: Double,
    val homeNet: Double = 0.0,
    val awayNet: Double = 0.0,
    val formulaChoiceH: Map<Int, Int> = emptyMap(),
    val result: Int = 0,
    val _id: Id<NbaMatchup> = newId()
)
