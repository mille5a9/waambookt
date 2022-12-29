package org.waambokt.service.odds.models

data class Market(
    val market: String,
    val outcomes: List<Outcome>
)
