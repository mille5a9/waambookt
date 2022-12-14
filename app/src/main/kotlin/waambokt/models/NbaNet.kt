package waambokt.models

import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

data class NbaNet(
    val teamName: String,
    val netValue: Double,
    val updated: Date = Date.from(Instant.now().minus(5, ChronoUnit.HOURS)),
    val _id: Id<NbaNet> = newId()
)
