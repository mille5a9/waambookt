package waambokt.data

data class ReprimandLog(
    val userId: ULong,
    val count: Int,
    val reasons: List<String>
)
