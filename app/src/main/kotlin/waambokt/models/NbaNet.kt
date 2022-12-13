package waambokt.models

import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.Date

data class NbaNet(
    val teamName: String,
    val netValue: Double,
    val updated: Date = Date(),
    val _id: Id<NbaNet> = newId()
)
