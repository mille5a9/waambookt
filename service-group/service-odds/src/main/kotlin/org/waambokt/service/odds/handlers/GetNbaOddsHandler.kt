package org.waambokt.service.odds.handlers

import com.google.protobuf.Timestamp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import mu.KotlinLogging
import org.json.JSONArray
import org.json.JSONObject
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.descending
import org.waambokt.common.constants.Environment
import org.waambokt.common.extensions.TimestampExtension.getInstant
import org.waambokt.common.models.NbaGameSpreads
import org.waambokt.service.odds.extensions.JSONArrayExtension.mapEvents
import org.waambokt.service.odds.models.Bookmaker
import org.waambokt.service.odds.models.Event
import org.waambokt.service.odds.models.Outcome
import org.waambokt.service.spec.odds.Bet
import org.waambokt.service.spec.odds.NbaOdds
import org.waambokt.service.spec.odds.NbaOddsRequest
import org.waambokt.service.spec.odds.NbaOddsResponse
import org.waambokt.service.spec.score.GameResult.Team.TeamEnum
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class GetNbaOddsHandler constructor(
    private val envars: Environment,
    private val dbClient: CoroutineDatabase
) {
    suspend fun handle(request: NbaOddsRequest): NbaOddsResponse {
        val markets = request.oddsMarketsList.joinToString(",") { it.name.lowercase() }

        // short circuit if there exists logged odds for today
        val loadedOdds = loadOdds()
        if (markets == "spreads" && loadedOdds != NbaOddsResponse.getDefaultInstance()) return loadedOdds

        val books = "fanduel,draftkings,betmgm,pointsbetus,betrivers,barstool,wynnbet,williamhill_us"
        val requestStr = "$baseUrl/basketball_nba/odds/?apiKey=${envars["ODDS"]}&markets=$markets&bookmakers=$books"
        logger.info { requestStr }
        val oddsResponse = HttpClient().get(requestStr)
//        val fakeResponse = Resources.getResource("api-odds.json").readText()

        val grpcResponse = NbaOddsResponse.newBuilder()
            .addAllGames(
                JSONArray(oddsResponse.body<String>()).mapEvents().flatMap {
//                JSONArray(fakeResponse).mapEvents().flatMap {
                    request.oddsMarketsList.map { enum ->
                        val market = enum.name.lowercase()
                        val espnGameId = getEspnGameId(it)
                        NbaOdds.newBuilder()
                            .setGameId(espnGameId)
                            .setHomeTeamName(it.home)
                            .setAwayTeamName(it.away)
                            .setHomeOrOver(
                                it.bookmakers.getBestBet(
                                    market,
                                    if (market == "totals") "Over" else it.home
                                )
                            )
                            .setAwayOrUnder(
                                it.bookmakers.getBestBet(
                                    market,
                                    if (market == "totals") "Under" else it.away
                                )
                            )
                            .setTime(
                                Timestamp.newBuilder()
                                    .setSeconds(Instant.parse(it.time).minusSeconds(3600 * 5).epochSecond)
                                    .setNanos(0)
                                    .build()
                            )
                            .build()
                    }
                }
            ).build()
        saveOdds(grpcResponse)

        logger.info { (grpcResponse.toString()) }

        return grpcResponse
    }

    private suspend fun getEspnGameId(event: Event): Int {
        val date = LocalDate.ofInstant(Instant.parse(event.time).minusSeconds(3600 * 5), ZoneId.of("UTC"))
            .format(DateTimeFormatter.BASIC_ISO_DATE)
        val requestStr = "https://site.api.espn.com/apis/site/v2/sports/basketball/nba/scoreboard?dates=$date"
        logger.info { requestStr }
        val json = JSONObject(HttpClient().get(requestStr).body<String>()).getJSONArray("events")
        return List<Pair<String, Int>>(json.length()) { index ->
            json.getJSONObject(index).let {
                Pair(
                    it.getString("name"),
                    it.getInt("id")
                )
            }
        }.find { x ->
            x.first.split(" at ") == listOf(event.away, event.home)
        }?.second ?: 0
    }

    private suspend fun loadOdds(): NbaOddsResponse {
        logger.info { "loading odds db" }
        val oddsLog =
            dbClient.getCollection<NbaGameSpreads>().find().sort(descending(NbaGameSpreads::time)).limit(15).toList()
                .filter { LocalDate.ofInstant(it.time, ZoneId.of("UTC")).toEpochDay() == LocalDate.now().toEpochDay() }
        return if (oddsLog.isEmpty()) NbaOddsResponse.getDefaultInstance()
        else {
            logger.info { "building loaded odds, skipping api" }
            NbaOddsResponse.newBuilder()
                .addAllGames(
                    oddsLog.map {
                        NbaOdds.newBuilder()
                            .setGameId(it.gameId)
                            .setHomeTeamName(getTeamNameFromEnum(it.homeTeamId))
                            .setAwayTeamName(getTeamNameFromEnum(it.awayTeamId))
                            .setHomeOrOver(
                                Bet.newBuilder()
                                    .setMarket("spreads")
                                    .setBestBook(it.homeBook)
                                    .setBestOdds(it.homePrice)
                                    .setBestLine(it.homeSpread)
                                    .build()
                            )
                            .setAwayOrUnder(
                                Bet.newBuilder()
                                    .setMarket("spreads")
                                    .setBestBook(it.awayBook)
                                    .setBestOdds(it.awayPrice)
                                    .setBestLine(it.awaySpread)
                                    .build()
                            )
                            .setTime(
                                Timestamp.newBuilder()
                                    .setSeconds(it.time.epochSecond)
                                    .setNanos(0)
                                    .build()
                            )
                            .build()
                    }
                )
                .build()
        }
    }

    private suspend fun saveOdds(odds: NbaOddsResponse) {
        val oddsLog = dbClient.getCollection<NbaGameSpreads>()
        odds.gamesList.forEach {
            oddsLog.save(
                NbaGameSpreads(
                    it.gameId,
                    it.time.getInstant(),
                    TeamEnum.valueOf(it.homeTeamName.replace(' ', '_')).number,
                    it.homeOrOver.bestLine,
                    it.homeOrOver.bestOdds,
                    it.homeOrOver.bestBook,
                    TeamEnum.valueOf(it.awayTeamName.replace(' ', '_')).number,
                    it.awayOrUnder.bestLine,
                    it.awayOrUnder.bestOdds,
                    it.awayOrUnder.bestBook
                )
            )
        }
    }

    private fun getTeamNameFromEnum(number: Int) =
        TeamEnum.forNumber(number).name.replace('_', ' ')

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
