package org.waambokt.service.net.handlers

import com.google.protobuf.Timestamp
import it.skrape.core.htmlDocument
import it.skrape.fetcher.AsyncFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.a
import it.skrape.selects.html5.table
import it.skrape.selects.html5.tbody
import it.skrape.selects.html5.td
import it.skrape.selects.html5.tr
import mu.KotlinLogging
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.waambokt.common.constants.Nba
import org.waambokt.service.net.models.NbaNet
import org.waambokt.service.spec.net.FormulaRequest
import org.waambokt.service.spec.net.FormulaResponse
import org.waambokt.service.spec.net.FormulaResult
import org.waambokt.service.spec.net.FormulaResult.FormulaChoiceEnum
import org.waambokt.service.spec.odds.Bet
import org.waambokt.service.spec.odds.NbaOdds
import org.waambokt.service.spec.odds.NbaOddsRequest
import org.waambokt.service.spec.odds.NbaOddsResponse
import org.waambokt.service.spec.odds.OddsServiceGrpcKt
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class GetFormulationHandler(
    private val db: CoroutineDatabase,
    private val oddsService: OddsServiceGrpcKt.OddsServiceCoroutineStub
) {
    private val dayms = 86400000

    suspend fun handle(request: FormulaRequest): FormulaResponse {
        return when (request.formula) {
            FormulaRequest.FormulasEnum.NET_3MINUS_2 -> net3minus2()
            else -> FormulaResponse.getDefaultInstance()
        }
    }

    private suspend fun net3minus2(): FormulaResponse {
        val spreads = getBestSpreads()
        val nets = refreshNet()
        return FormulaResponse.newBuilder().addAllFormulaResults(
            spreads.gamesList.mapNotNull {
                if (it.time.getDays() != LocalDate.now().toEpochDay()) return@mapNotNull null
                val choice = it.makeChoice(nets)
                val side = when (choice) {
                    FormulaChoiceEnum.HOME_SPREAD -> Pair(it.homeTeamName, it.homeOrOver)
                    FormulaChoiceEnum.AWAY_SPREAD -> Pair(it.awayTeamName, it.awayOrUnder)
                    else -> Pair("${it.awayTeamName} @ ${it.homeTeamName}", Bet.getDefaultInstance())
                }
                FormulaResult.newBuilder()
                    .setChoice(choice)
                    .setResult(FormulaResult.SpreadResultEnum.SPREAD_RESULT_ENUM_UNSPECIFIED)
                    .setGameId(it.gameId)
                    .setName(side.first)
                    .setLine(side.second.bestLine)
                    .setOdds(side.second.bestOdds)
                    .setBook(side.second.bestBook)
                    .build()
            }
        ).build()
    }

    private fun Timestamp.getDays() =
        LocalDate.ofInstant(Instant.ofEpochSecond(this.seconds), ZoneId.of("UTC")).toEpochDay()

    private fun NbaOdds.makeChoice(nets: List<NbaNet>): FormulaChoiceEnum {
        val homeNet = nets.findTeamNet(homeTeamName)
        val awayNet = nets.findTeamNet(awayTeamName)
        val homeSpreadDiff = (homeOrOver.bestLine + Nba.min) - (awayNet - homeNet - 3)
        val awaySpreadDiff = (awayOrUnder.bestLine + Nba.min) - (3 + homeNet - awayNet)
        return if (homeSpreadDiff > 0 && awaySpreadDiff > 0) FormulaChoiceEnum.NO_CONTEST
        else if (homeSpreadDiff > awaySpreadDiff) FormulaChoiceEnum.HOME_SPREAD
        else FormulaChoiceEnum.AWAY_SPREAD
    }

    private fun List<NbaNet>.findTeamNet(name: String) =
        this.find { x -> x.teamName == name }?.netValue ?: 0.0

    private suspend fun getBestSpreads(): NbaOddsResponse {
        return oddsService.getNbaOdds(
            NbaOddsRequest.newBuilder()
                .addOddsMarkets(NbaOddsRequest.NbaOddsMarketsEnum.SPREADS)
                .build()
        )
    }

    // Scrapes and updates the nba NET information in the db,
    // then returns a map of relevant values for command execution
    private suspend fun refreshNet(): List<NbaNet> {
        val netsCollection = db.getCollection<NbaNet>("nbaNet")
        val allNets = netsCollection.find().toList()

        // short circuit if they were already updated today
        if (allNets.isNotEmpty() && allNets[0].updated.time.floorDiv(dayms) == Date().time.floorDiv(dayms)) {
            logger.info("using cached net ratings")
            return allNets
        }

        // scrape new nets and update table
        return scrapeNetsAndCommit(netsCollection)
    }

    private suspend fun scrapeNetsAndCommit(
        netsCollection: CoroutineCollection<NbaNet>
    ): List<NbaNet> {
        logger.info("scraping net ratings")
        val allNets =
            skrape(AsyncFetcher) {
                request {
                    url = "https://www.basketball-reference.com/leagues/NBA_2023_ratings.html"
                }
                response {
                    htmlDocument {
                        table("#ratings") {
                            tbody {
                                tr {
                                    findAll {
                                        map {
                                            NbaNet(
                                                it.td {
                                                    withAttribute = "data-stat" to "team_name"
                                                    findFirst {
                                                        this.a {
                                                            findFirst { text }
                                                        }
                                                    }
                                                },
                                                it.td {
                                                    withAttribute = "data-stat" to "net_rtg_adj"
                                                    findFirst { text.toDouble() }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        println(allNets)

        // wipe old nets
        netsCollection.deleteMany()

        // add new nets
        netsCollection.insertMany(allNets)

        return allNets
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
