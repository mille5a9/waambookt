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
import waambokt.constants.Nba.HowToBetEnum
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
        val decisions = fetchMatchups(refreshNet()).map {
            Pair(it, calculateBet(it))
        }
        val output =
            decisions.map {
                if (abs(it.first.homeSpread) == 0.01) {
                    return@map "${it.first.homeName} @ ${it.first.awayName} is in progress or finished, no bet"
                }

                return@map when (it.second) {
                    HowToBetEnum.HOME_SPREAD -> "${Nba.abbr[it.first.homeName]} ${it.first.homeSpread}"
                    HowToBetEnum.AWAY_SPREAD -> "${Nba.abbr[it.first.awayName]} ${it.first.homeSpread * -1}"
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
            FormulaEnum.NET3MINUS2.ordinal -> calculateNet3Minus2(matchup)
            else -> HowToBetEnum.NO_CONTEST
        }
    }

    private fun calculateNet3Minus2(matchup: NbaMatchup): HowToBetEnum {
        val implSpreadH = matchup.awayNet - matchup.homeNet - 3
        if (implSpreadH < (matchup.homeSpread - Nba.min)) {
            return HowToBetEnum.HOME_SPREAD
        } else if (implSpreadH > (matchup.homeSpread + Nba.min)) {
            return HowToBetEnum.AWAY_SPREAD
        }
        return HowToBetEnum.NO_CONTEST
    }

    private enum class FormulaEnum {
        NET3MINUS2
    }

    private fun List<NbaNet>.findNet(team: String) =
        this.find { it.teamName == team }?.netValue ?: throw NoSuchElementException()

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

    private fun String.odds(homeAbbr: String) =
        this.toDouble() * if (this.split(' ').first() == homeAbbr) 1 else -1

    private fun JSONArray.getGameId(index: Int) =
        this.getJSONObject(index).getString("id")

    // helpers to decrease verbosity of the json-parsing code
    private fun JSONArray.getTeam(index: Int, team: Int) =
        this.getJSONObject(index).getString("shortName").split(" @ ")[team]

    private fun JSONArray.getOdds(index: Int): String {
        return this.getJSONObject(index).getJSONArray("competitions").getJSONObject(0)
            .optJSONArray("odds")?.getJSONObject(0)?.getString("details") ?: "-0.01"
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
