package org.waambokt.service.odds.handlers

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.json.JSONArray
import org.json.JSONObject
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.waambokt.common.constants.Environment
import org.waambokt.service.spec.odds.Bet
import org.waambokt.service.spec.odds.NbaOdds
import org.waambokt.service.spec.odds.NbaOddsRequest
import org.waambokt.service.spec.odds.NbaOddsResponse
import kotlin.math.abs
class GetNbaOddsHandler constructor(
    private val db: CoroutineDatabase,
    private val envars: Environment
) {
    suspend fun handle(request: NbaOddsRequest): NbaOddsResponse {
        val markets = request.oddsMarketsList.joinToString(",") { it.name.lowercase() }
        val books = "fanduel,draftkings,betmgm,foxbet,pointsbetus,betrivers,barstool,wynnbet,williamhill_us,betonlineag"
        val oddsResponse = HttpClient()
            .get("$baseUrl/basketball_nba/odds/?apiKey=${envars["ODDS"]}&markets=$markets&bookmakers=$books")
        val grpcResponse = NbaOddsResponse.newBuilder()
        JSONArray(oddsResponse.body<String>()).mapEvents().forEach {
            for (oddsMarketEnum in request.oddsMarketsList) {
                grpcResponse.addGames(
                    NbaOdds.newBuilder()
                        .setGameId(it.id)
                        .setHomeTeamName(it.home)
                        .setAwayTeamName(it.away)
                        .setHomeOption(it.getBestBet(true), oddsMarketEnum.name.lowercase())
                        .setAwayOption(it.getBestBet(false), oddsMarketEnum.name.lowercase())
                )
            }
        }

        return NbaOddsResponse.getDefaultInstance()
    }

    private fun Event.getBestBet(isHome: Boolean, market: String): Bet {
        this.bookmakers.reduce { acc, bookmaker ->
            if (
                abs(acc.markets.find {
                    it.market == market
                }?.outcomes?.find {
                    if (isHome) it.name == this.home else it.name == this.away
                }?.point ?: 0.0) < kotlin.math.abs(bookmaker.markets.find {
                    it.market == market
                }?.outcomes?.find {
                    if (isHome) it.name == this.home else it.name == this.away
                }?.point ?: 0.0)
            )
        }
    }

    private fun JSONArray.mapEvents() = this.map { any ->
        JSONObject(any).let {
            Event(
                it.getString("id"),
                it.getString("commence_time"),
                it.getString("home_team"),
                it.getString("away_team"),
                it.getJSONArray("bookmakers").mapBookmakers()
            )
        }
    }

    private fun JSONArray.mapBookmakers() = this.map { any ->
        JSONObject(any).let {
            Bookmaker(
                it.getString("key"),
                it.getString("title"),
                it.getJSONArray("markets").mapMarkets()
            )
        }
    }

    private fun JSONArray.mapMarkets() = this.map { any ->
        JSONObject(any).let {
            Market(
                it.getString("key"),
                it.getJSONArray("outcomes").mapOutcomes()
            )
        }
    }

    private fun JSONArray.mapOutcomes() = this.map { any ->
        JSONObject(any).let {
            Outcome(
                it.getString("name"),
                it.getDouble("price"),
                it.getDouble("point")
            )
        }
    }

    companion object {
        private const val baseUrl = "https://api.the-odds-api.com/v4/sports"

        data class Event(
            val id: String,
            val time: String,
            val home: String,
            val away: String,
            val bookmakers: List<Bookmaker>
        )

        data class Bookmaker(
            val key: String,
            val title: String,
            val markets: List<Market>
        )

        data class Market(
            val market: String,
            val outcomes: List<Outcome>
        )

        data class Outcome(
            val name: String,
            val price: Double,
            val point: Double
        )
    }
}
