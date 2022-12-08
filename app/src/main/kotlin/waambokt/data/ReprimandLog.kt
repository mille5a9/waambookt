package waambokt.data

import org.litote.kmongo.Id
import org.litote.kmongo.newId

data class ReprimandLog(
    val userId: String,
    val count: Int = 1,
    val reasons: List<String> = emptyList(),
    val _id: Id<ReprimandLog> = newId()
)
