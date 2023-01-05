package org.waambokt.service.score.handlers

import com.google.protobuf.Timestamp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import mu.KotlinLogging
import org.json.JSONArray
import org.json.JSONObject
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.waambokt.common.extensions.TimestampExtension.getDays
import org.waambokt.common.extensions.TimestampExtension.getInstant
import org.waambokt.common.models.NbaBox
import org.waambokt.common.models.NbaBox.BoxTeam
import org.waambokt.service.spec.net.NetServiceGrpcKt
import org.waambokt.service.spec.net.SingleNetRequest
import org.waambokt.service.spec.score.GameResult
import org.waambokt.service.spec.score.GameResult.Team
import org.waambokt.service.spec.score.StoreDayGamesRequest
import org.waambokt.service.spec.score.StoreDayGamesResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StoreDayGamesHandler(
    private val db: CoroutineDatabase,
    private val netService: NetServiceGrpcKt.NetServiceCoroutineStub
) {
    suspend fun handle(request: StoreDayGamesRequest): StoreDayGamesResponse {
        val date = LocalDate.ofEpochDay(request.day.getDays()).format(DateTimeFormatter.BASIC_ISO_DATE)

        val requestStr = "https://site.api.espn.com/apis/site/v2/sports/basketball/nba/scoreboard?dates=$date"
        logger.info { requestStr }
        val json = JSONObject(HttpClient().get(requestStr).body<String>())
        return json.getJSONArray("events").mapEvents(request.day.getDays()).let {
            saveGames(it)
            StoreDayGamesResponse.newBuilder()
                .addAllGameResult(it)
                .build()
        }
    }

    private suspend fun JSONArray.mapEvents(days: Long) = List(this.length()) {
        this.getJSONObject(it).let { obj ->
            val gameId = obj.getInt("id")
            val requestStr = "https://site.api.espn.com/apis/site/v2/sports/basketball/nba/summary?event=$gameId"
            logger.info { requestStr }
            val boxscore = JSONObject(HttpClient().get(requestStr).body<String>()).getJSONObject("boxscore")
            GameResult.newBuilder()
                .setGameId(gameId)
                .setTime(Timestamp.newBuilder().setSeconds(days * 24 * 3600).build())
                .setHome(getTeam(boxscore.getJSONArray("teams").getJSONObject(1)))
                .setAway(getTeam(boxscore.getJSONArray("teams").getJSONObject(0)))
                .build()
        }
    }

    private suspend fun getTeam(team: JSONObject): Team {
        val statistics = team.getJSONArray("statistics")
        val teamName = team.getTeamName()
        val fgmFga = statistics.getStats(0)
        val ftmFta = statistics.getStats(4)
        val threesMade = statistics.getStats(2).first().toInt()
        return Team.newBuilder()
            .setTeamId(Team.TeamEnum.valueOf(teamName.replace(' ', '_')))
            .setFg(fgmFga.first().toInt())
            .setFga(fgmFga.last().toInt())
            .setFt(ftmFta.first().toInt())
            .setFta(ftmFta.last().toInt())
            .setFg3P(threesMade)
            .setOreb(statistics.getStat(7))
            .setDreb(statistics.getStat(8))
            .setTurnovers(statistics.getStat(12))
            .setAdjNet(getSingleNet(teamName))
            .setScore(fgmFga.first().toInt().times(2).plus(threesMade))
            .build()
    }

    private suspend fun getSingleNet(teamName: String) =
        netService.getSingleNet(SingleNetRequest.newBuilder().setTeamName(teamName).build()).netValue

    private fun JSONObject.getTeamName() = this.getJSONObject("team").getString("displayName")

    private fun JSONArray.getStats(index: Int) = this.getJSONObject(index).getString("displayValue").split('-')

    private fun JSONArray.getStat(index: Int) = this.getJSONObject(index).getInt("displayValue")

    private suspend fun saveGames(games: List<GameResult>) {
        val nbaBoxCollection = db.getCollection<NbaBox>()
        games.forEach {
            nbaBoxCollection.save(
                NbaBox(
                    it.gameId,
                    it.time.getInstant(),
                    it.home.buildBoxTeam(),
                    it.away.buildBoxTeam()
                )
            )
        }
    }

    private fun Team.buildBoxTeam() =
        BoxTeam(
            this.teamId.number,
            this.fg,
            this.fg3P,
            this.fga,
            this.turnovers,
            this.ft,
            this.fta,
            this.oreb,
            this.dreb,
            this.adjNet,
            this.score
        )

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
