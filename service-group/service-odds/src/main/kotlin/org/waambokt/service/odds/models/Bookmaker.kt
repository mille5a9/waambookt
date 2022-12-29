package org.waambokt.service.odds.models

data class Bookmaker(
    val key: String,
    val title: String,
    val markets: List<Market>
)
