package org.waambokt.service.odds.models

data class Event(
    val id: String,
    val time: String,
    val home: String,
    val away: String,
    val bookmakers: List<Bookmaker>
)
