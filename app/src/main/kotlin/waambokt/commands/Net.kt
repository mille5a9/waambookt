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
import waambokt.constants.Nba
import waambokt.models.NbaNet
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date

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
        val nets = refreshNet()
        val output =
            fetchMatchups().map { temp ->
                if (temp.third == "null") {
                    return@map "${temp.first} @ ${temp.second} is in progress or finished, no bet"
                }

                val oddsInfo = temp.getOdds()
                val netValues = nets.findNets(temp.first, temp.second)

                return@map when (calculateBet(netValues, oddsInfo.second)) {
                    HowToBetEnum.HOME_SPREAD -> "${Nba.abbr[temp.second]} ${oddsInfo.second}"
                    HowToBetEnum.AWAY_SPREAD -> "${Nba.abbr[temp.first]} ${oddsInfo.first}"
                    HowToBetEnum.NO_CONTEST ->
                        if (hideNoContests) "" else "Don't bet on ${temp.first} @ ${temp.second}"
                }.plus(
                    if (netOut) " (${temp.second}: ${netValues.second}, ${temp.first}: ${netValues.first})" else ""
                )
            }
        return output.filter { it.isNotEmpty() }.joinToString("\n", "```", "```")
    }

    private fun calculateBet(nets: Pair<Double, Double>, homeSpread: Double): HowToBetEnum {
        return when (formula) {
            FormulaEnum.NET3MINUS2.ordinal -> calculateNet3Minus2(nets, homeSpread)
            else -> HowToBetEnum.NO_CONTEST
        }
    }

    private fun calculateNet3Minus2(nets: Pair<Double, Double>, homeSpread: Double): HowToBetEnum {
        val implSpreadH = nets.first - nets.second - 3
        if (implSpreadH < (homeSpread - Nba.min)) {
            return HowToBetEnum.HOME_SPREAD
        } else if (implSpreadH > (homeSpread + Nba.min)) {
            return HowToBetEnum.AWAY_SPREAD
        }
        return HowToBetEnum.NO_CONTEST
    }

    private enum class HowToBetEnum {
        HOME_SPREAD,
        AWAY_SPREAD,
        NO_CONTEST
    }

    private enum class FormulaEnum {
        NET3MINUS2
    }

    private fun List<NbaNet>.findNets(away: String, home: String) = Pair(this.findNet(away), this.findNet(home))

    private fun List<NbaNet>.findNet(team: String) =
        this.find { it.teamName == Nba.abbr[team] }?.netValue ?: throw NoSuchElementException()

    private fun Triple<String, String, String>.getOdds(): Pair<Double, Double> {
        val oddsSubstr = this.third.split(' ')
        if (oddsSubstr.size == 1) return Pair(0.0, 0.0)
        return if (this.first == oddsSubstr[0]) oddsSubstr[1].odds(true) else oddsSubstr[1].odds(false)
    }

    private fun String.odds(homeFav: Boolean) =
        Pair(this.toDouble() * if (homeFav) 1 else -1, this.toDouble() * if (homeFav) -1 else 1)

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

    private suspend fun fetchMatchups(): List<Triple<String, String, String>> {
        logger.info("fetching matchups")
        val today = Date().toInstant().atOffset(ZoneOffset.UTC).format(
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.ofHours(-5))
        )
        val response = HttpClient()
            .get("https://site.api.espn.com/apis/site/v2/sports/basketball/nba/scoreboard?dates=$today")
        val gamesList = JSONObject(response.body<String>()).getJSONArray("events")
        return List(gamesList.length()) {
            Triple(gamesList.getTeam(it, 0), gamesList.getTeam(it, 1), gamesList.getOdds(it))
        }
    }

    // helpers to decrease verbosity of the json-parsing code
    private fun JSONArray.getTeam(index: Int, team: Int) =
        this.getJSONObject(index).getString("shortName").split(" @ ")[team]

    private fun JSONArray.getOdds(index: Int): String {
        return this.getJSONObject(index).getJSONArray("competitions").getJSONObject(0)
            .optJSONArray("odds")?.getJSONObject(0)?.getString("details") ?: "null"
    }

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
