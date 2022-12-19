package waambokt.commands

import com.mongodb.reactivestreams.client.MongoDatabase
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
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
import org.json.JSONArray
import org.json.JSONObject
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.ne
import waambokt.constants.Nba
import waambokt.constants.Nba.HowToBetEnum
import waambokt.constants.Nba.NetResultsEnum
import waambokt.extensions.DoubleExtension.convertPercent
import waambokt.extensions.NbaMatchupExtension.net3Minus2
import waambokt.extensions.PairExtension.valEq
import waambokt.models.NbaMatchup
import waambokt.models.NbaNet
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.math.abs

class Net
private constructor(
    private val mongo: CoroutineDatabase,
    private val event: ChatInputCommandInteractionCreateEvent,
    private val formula: Int,
    private val netOut: Boolean,
    private val hideNoContests: Boolean
) : Command() {
    private val dayms = 86400000

    override suspend fun respond() {
        logger.info("respond net")
        val response = event.interaction.deferEphemeralResponse()
        response.respond { this.content = execute() }
    }

    override suspend fun execute(): String {
        logger.info("execute net")
        if (formula == -1) return updateIndGameResults()

        val decisions = fetchMatchups(refreshNet()).map {
            Pair(it, calculateBet(it))
        }
        val output =
            decisions.map {
                if (abs(it.first.homeSpread) == 0.01) {
                    return@map "${it.first.homeName} @ ${it.first.awayName} is in progress or finished, no bet"
                }

                return@map when (it.second) {
                    HowToBetEnum.HOME_SPREAD -> "${it.first.homeName} ${it.first.homeSpread}"
                    HowToBetEnum.AWAY_SPREAD -> "${it.first.awayName} ${it.first.homeSpread * -1}"
                    HowToBetEnum.NO_CONTEST ->
                        if (hideNoContests) "" else "Don't bet on ${it.first.awayName} @ ${it.first.homeName}"
                }.plus(
                    if (netOut) {
                        " (${it.first.homeName}: ${it.first.homeNet}, ${it.first.awayName}: ${it.first.awayNet})"
                    } else ""
                )
            }
        decisions.saveMatchups()
        return output.filter { it.isNotEmpty() }.joinToString("\n", "```", "```")
    }

    private suspend fun List<Pair<NbaMatchup, HowToBetEnum>>.saveMatchups() {
        val matchupsCollection = mongo.getCollection<NbaMatchup>("nbaMatchup")
        this.forEach {
            val newMap = it.first.formulaChoiceH.plus(formula to it.second.ordinal)
            matchupsCollection.save(
                it.first.copy(formulaChoiceH = newMap)
            )
        }
    }

    private fun calculateBet(matchup: NbaMatchup): HowToBetEnum {
        return when (formula) {
            FormulaEnum.NET3MINUS2.ordinal -> matchup.net3Minus2()
            else -> HowToBetEnum.NO_CONTEST
        }
    }

    private enum class FormulaEnum {
        NET3MINUS2
    }

    private val formulaNames = mapOf(
        FormulaEnum.NET3MINUS2.ordinal to "Net3Minus2"
    )

    // Scrapes and updates the nba NET information in the db,
    // then returns a map of relevant values for command execution
    private suspend fun refreshNet(): List<NbaNet> {
        val netsCollection = mongo.getCollection<NbaNet>("nbaNet")
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
                                                            findFirst { text.split(' ').last() }
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

    private suspend fun fetchMatchups(nets: List<NbaNet>): List<NbaMatchup> {
        logger.info("fetching matchups")
        val today = Date().toInstant().atOffset(ZoneOffset.UTC).format(
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.ofHours(-5))
        )
        val response = HttpClient()
            .get("https://site.api.espn.com/apis/site/v2/sports/basketball/nba/scoreboard?dates=$today")
        val gamesList = JSONObject(response.body<String>()).getJSONArray("events")
        return List(gamesList.length()) {
            val homeAbbr = gamesList.getTeam(it, 1)
            val awayAbbr = gamesList.getTeam(it, 0)
            val homeSpread = gamesList.getOdds(it).odds(homeAbbr)
            NbaMatchup(
                gamesList.getGameId(it),
                Nba.abbr[homeAbbr] ?: "",
                Nba.abbr[awayAbbr] ?: "",
                homeSpread,
                nets.findNet(homeAbbr),
                nets.findNet(awayAbbr)
            )
        }
    }

    private fun List<NbaNet>.findNet(team: String) =
        this.find { it.teamName == Nba.abbr[team] }?.netValue ?: throw NoSuchElementException()

    private fun String.odds(homeAbbr: String) =
        this.substringAfter(' ').toDouble() * if (this.substringBefore(' ') == homeAbbr) 1 else -1

    private fun JSONArray.getGameId(index: Int) =
        this.getJSONObject(index).getString("id")

    // helpers to decrease verbosity of the json-parsing code
    private fun JSONArray.getTeam(index: Int, team: Int) =
        this.getJSONObject(index).getString("shortName").split(" @ ")[team]

    private fun JSONArray.getOdds(index: Int): String {
        return this.getJSONObject(index).getJSONArray("competitions").getJSONObject(0)
            .optJSONArray("odds")?.getJSONObject(0)?.getString("details") ?: "-0.01"
    }

    private suspend fun updateIndGameResults(): String {
        val matchupsCollection = mongo.getCollection<NbaMatchup>("nbaMatchup")
        val indeterminates = matchupsCollection.find(NbaMatchup::result eq 0).toList()
        var updatedCount = 0
        for (indeterminate in indeterminates) {
            val scoreDiff = getScoreDiff(indeterminate.gameId)
            if (scoreDiff == 0) continue else updatedCount += 1
            matchupsCollection.save(
                indeterminate.copy(
                    result = if (indeterminate.homeSpread > scoreDiff) NetResultsEnum.HOME_WIN.ordinal
                    else if (indeterminate.homeSpread < scoreDiff) NetResultsEnum.AWAY_WIN.ordinal
                    else NetResultsEnum.DRAW.ordinal
                )
            )
        }
        return "Updated $updatedCount record with final score(s)\n${runNumbers(matchupsCollection)}"
    }

    // gets away score minus home score (effectively the resulting home spread)
    private suspend fun getScoreDiff(gameId: String): Int {
        val response = HttpClient()
            .get("https://site.api.espn.com/apis/site/v2/sports/basketball/nba/summary?event=$gameId")
        val competition = JSONObject(response.body<String>()).getJSONObject("header")
            .getJSONArray("competitions").getJSONObject(0)
        return if (!competition.getJSONObject("status").getJSONObject("type").getBoolean("completed")) 0
        else competition.getJSONArray("competitors").getJSONObject(1).getString("score").toInt() -
            competition.getJSONArray("competitors").getJSONObject(0).getString("score").toInt()
    }

    private suspend fun runNumbers(matchupsCollection: CoroutineCollection<NbaMatchup>): String {
        val allFinishedGames = matchupsCollection.find(NbaMatchup::result ne 0).toList()
        return FormulaEnum.values().joinToString("\n", "```", "```") {
            outPercentsForFormula(it.ordinal, allFinishedGames)
        }
    }

    private fun outPercentsForFormula(formulaEnumValue: Int, games: List<NbaMatchup>): String {
        val results = games.map { Pair(it.formulaChoiceH[formulaEnumValue] ?: 0, it.result) }
        val total = percentTotal(results)
        val home = percentHome(results)
        val away = percentAway(results)
        val fav = percentFav(formulaEnumValue, games, true)
        val dog = percentFav(formulaEnumValue, games, false)
        return "${formulaNames[formulaEnumValue]} Rates of Success:\n" +
            "Total: ${total.first.convertPercent()}% (${total.second})\n" +
            "Home: ${home.first.convertPercent()}% (${home.second})\n" +
            "Away: ${away.first.convertPercent()}% (${away.second})\n" +
            "Favorite: ${fav.first.convertPercent()}% (${fav.second})\n" +
            "Underdog: ${dog.first.convertPercent()}% (${dog.second})\n"
    }

    private fun percentTotal(data: List<Pair<Int, Int>>) =
        Pair(data.count { it.valEq(1, 2) || it.valEq(0, 1) }.toDouble() / data.count(), data.count())

    private fun percentHome(data: List<Pair<Int, Int>>) =
        Pair(
            data.count { it.valEq(0, 1) }.toDouble() / data.count { it.valEq(0, 1) || it.valEq(0, 2) },
            data.count { it.valEq(0, 1) || it.valEq(0, 2) }
        )

    private fun percentAway(data: List<Pair<Int, Int>>) =
        Pair(
            data.count { it.valEq(1, 2) }.toDouble() / data.count { it.valEq(1, 1) || it.valEq(1, 2) },
            data.count { it.valEq(1, 2) || it.valEq(1, 1) }
        )

    private fun percentFav(formulaEnumValue: Int, games: List<NbaMatchup>, isFav: Boolean): Pair<Double, Int> {
        val results = favBools(games, formulaEnumValue, isFav)
        return Pair(results.count { it }.toDouble() / results.count(), results.count())
    }

    private fun favBools(
        games: List<NbaMatchup>,
        formulaEnumValue: Int,
        resultsForFav: Boolean

    ) = games.mapNotNull {
        if ((
            Pair(it.formulaChoiceH[formulaEnumValue] ?: 0, it.result).valEq(0, 1) &&
                it.homeSpread.findFav(true, resultsForFav)
            ) ||
            (
                Pair(it.formulaChoiceH[formulaEnumValue] ?: 0, it.result).valEq(1, 2) &&
                    it.homeSpread.findFav(false, resultsForFav)
                )
        ) true
        else if ((
            Pair(it.formulaChoiceH[formulaEnumValue] ?: 0, it.result).valEq(0, 1) &&
                it.homeSpread.findFav(false, resultsForFav)
            ) ||
            (
                Pair(it.formulaChoiceH[formulaEnumValue] ?: 0, it.result).valEq(1, 2) &&
                    it.homeSpread.findFav(false, resultsForFav)
                )
        ) false
        else null
    }

    private fun Double.findFav(isHome: Boolean, isFav: Boolean) =
        ((isHome && this < 0) || (!isHome && this > 0)) == isFav

    companion object {
        private val logger = KotlinLogging.logger {}

        operator fun invoke(
            db: MongoDatabase,
            event: ChatInputCommandInteractionCreateEvent,
            formula: Int? = null,
            netOut: Boolean? = null,
            hideNoContests: Boolean? = null
        ): Net {
            logger.info("building net")
            return Net(
                CoroutineDatabase(db),
                event,
                formula ?: event.interaction.command.integers["formula"]?.toInt() ?: 0,
                netOut ?: event.interaction.command.booleans["net_out"] ?: false,
                hideNoContests ?: event.interaction.command.booleans["hide_no_contests"] ?: true
            )
        }
    }
}
