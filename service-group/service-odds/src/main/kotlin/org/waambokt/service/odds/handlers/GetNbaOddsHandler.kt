package org.waambokt.service.odds.handlers

import com.google.common.io.Resources
import com.google.protobuf.Timestamp
import com.google.type.DateTime
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import mu.KotlinLogging
import org.json.JSONArray
import org.waambokt.common.constants.Environment
import org.waambokt.service.odds.extensions.JSONArrayExtension.mapEvents
import org.waambokt.service.odds.models.Bookmaker
import org.waambokt.service.odds.models.Outcome
import org.waambokt.service.spec.odds.Bet
import org.waambokt.service.spec.odds.NbaOdds
import org.waambokt.service.spec.odds.NbaOddsRequest
import org.waambokt.service.spec.odds.NbaOddsResponse
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class GetNbaOddsHandler constructor(
    private val envars: Environment
) {
    suspend fun handle(request: NbaOddsRequest): NbaOddsResponse {
        val markets = request.oddsMarketsList.joinToString(",") { it.name.lowercase() }
        val books = "fanduel,draftkings,betmgm,foxbet,pointsbetus,betrivers,barstool,wynnbet,williamhill_us,betonlineag"
        val requestStr = "$baseUrl/basketball_nba/odds/?apiKey=${envars["ODDS"]}&markets=$markets&bookmakers=$books"
        logger.info { requestStr }
//        val oddsResponse = HttpClient().get(requestStr)
        val fakeResponse = Resources.getResource("api-odds.json").readText()

        val grpcResponse = NbaOddsResponse.newBuilder()
//        JSONArray(oddsResponse.body<String>()).mapEvents().forEach {
        JSONArray(fakeResponse).mapEvents().forEach {
            for (oddsMarketEnum in request.oddsMarketsList) {
                val market = oddsMarketEnum.name.lowercase()
                grpcResponse.addGames(
                    NbaOdds.newBuilder()
                        .setGameId(it.id)
                        .setHomeTeamName(it.home)
                        .setAwayTeamName(it.away)
                        .setHomeOrOver(it.bookmakers.getBestBet(market, if (market == "totals") "Over" else it.home))
                        .setAwayOrUnder(it.bookmakers.getBestBet(market, if (market == "totals") "Under" else it.away))
                        .setTime(
                            Timestamp.newBuilder()
                                .setSeconds(Instant.parse(it.time).minusSeconds(3600 * 5).epochSecond)
                                .setNanos(0)
                                .build()
                        )
                        .build()
                )
            }
        }
        logger.info { (grpcResponse.toString()) }

        return grpcResponse.build()
    }

    private fun List<Bookmaker>.getBestBet(
        market: String,
        name: String
    ) = this.map {
        logger.info { "getBestBet mapping Bookmakers" }
        OutcomeMeta(
            it.title,
            it.findMarketOutcomes(market).findOutcome(name)
        )
    }.reduce { acc, book ->
        logger.info { "getBestBet reducing Bookmakers" }
        if (acc.outcome.point < book.outcome.point) book
        else if (acc.outcome.point > book.outcome.point) acc
        else if (acc.outcome.price < book.outcome.price) book
        else acc
    }.let {
        logger.info { "getBestBet building Bet" }
        Bet.newBuilder()
            .setMarket(market)
            .setBestBook(it.title)
            .setBestSide(it.outcome.name)
            .setBestLine(it.outcome.point)
            .setBestOdds(it.outcome.price)
            .build()
    }

    private fun Bookmaker.findMarketOutcomes(market: String) = this.markets.find { it.market == market }!!.outcomes

    private fun List<Outcome>.findOutcome(name: String) =
        this.find { it.name == name }!!.let {
            logger.info { "findOutcome building Outcome" }
            Outcome(it.name, it.price, if (it.point.isNaN()) 0.0 else it.point)
        }

    companion object {
        private const val baseUrl = "https://api.the-odds-api.com/v4/sports"

        private val logger = KotlinLogging.logger { }

        data class OutcomeMeta(
            val title: String,
            val outcome: Outcome
        )
    }
}
