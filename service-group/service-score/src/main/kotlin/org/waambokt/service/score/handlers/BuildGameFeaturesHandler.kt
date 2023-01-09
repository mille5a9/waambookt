package org.waambokt.service.score.handlers

import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.CoroutineFindPublisher
import org.litote.kmongo.eq
import org.waambokt.common.models.NbaBox
import org.waambokt.common.models.NbaFeatures
import org.waambokt.common.models.NbaFourFactors
import org.waambokt.common.models.NbaGameSpreads
import org.waambokt.service.spec.score.BuildGameFeaturesRequest
import org.waambokt.service.spec.score.BuildGameFeaturesResponse
import org.waambokt.service.spec.score.BuildGameFeaturesResponse.TeamFeatures
import java.time.Instant
import java.time.temporal.ChronoUnit

class BuildGameFeaturesHandler(
    private val db: CoroutineDatabase
) {
    suspend fun handle(request: BuildGameFeaturesRequest): BuildGameFeaturesResponse {
        val boxData = db.getCollection<NbaBox>().findOne(NbaBox::gameId eq request.gameId)!!
        val oddsData = db.getCollection<NbaGameSpreads>().findOne(NbaGameSpreads::gameId eq request.gameId)!!
        val factors = db.getCollection<NbaFourFactors>().find(NbaFourFactors::gameId eq request.gameId).limit(2)

        val response = BuildGameFeaturesResponse.newBuilder()
            .setGameId(request.gameId)
            .setHome(teamFeatures(oddsData, factors.getByTeamId(oddsData.homeTeamId)!!, boxData, true))
            .setAway(teamFeatures(oddsData, factors.getByTeamId(oddsData.awayTeamId)!!, boxData, false))
            .build()

        saveBothFeatures(response)

        return response
    }

    private suspend fun teamFeatures(
        oddsData: NbaGameSpreads,
        factors: NbaFourFactors,
        boxData: NbaBox,
        isHome: Boolean
    ): TeamFeatures? = TeamFeatures.newBuilder()
        .setTeamId(boxData.getTeamId(isHome))
        .setShooting(factors.shooting.coerceIn(0.0, 1.0))
        .setTurnovers(factors.turnovers.coerceIn(0.0, 1.0))
        .setRebounding(factors.rebounds.coerceIn(0.0, 1.0))
        .setFreebies(factors.freebies.coerceIn(0.0, 1.0))
        .setBackToBack(isTeamSleepy(boxData.getTeamId(isHome), boxData.getTeamId(!isHome), boxData.time))
        .setBestSpread(calcSpreadFeature(oddsData.homeSpread))
        .setResult(getResult(boxData, oddsData, isHome))
        .build()

    private fun getResult(boxData: NbaBox, oddsData: NbaGameSpreads, isHome: Boolean): Double {
        val scoreDiff = boxData.home.score - boxData.away.score
        return if (isHome) (0.5 + ((scoreDiff + oddsData.homeSpread) * 0.02)).coerceIn(0.0, 1.0)
        else (0.5 + ((scoreDiff * -1 + oddsData.awaySpread) * 0.02)).coerceIn(0.0, 1.0)
    }

    private fun NbaBox.getTeamId(isHome: Boolean) = if (isHome) this.home.teamId else this.away.teamId

    private fun calcSpreadFeature(spread: Double) = (0.5 - (spread * 0.02)).coerceIn(0.0, 1.0)

    private suspend fun isTeamSleepy(goodGuys: Int, badGuys: Int, time: Instant): Double {
        val dayBefore = time.minus(1, ChronoUnit.DAYS)
        val sleepyTeams = db.getCollection<NbaBox>().find(NbaBox::time eq dayBefore).toList()
        return if (sleepyTeams.find { x -> x.home.teamId == goodGuys } != null) 1.0
        else if (sleepyTeams.find { x -> x.away.teamId == badGuys } != null) 0.0
        else 0.5
    }

    private suspend fun CoroutineFindPublisher<NbaFourFactors>.getByTeamId(id: Int) =
        this.filter(NbaFourFactors::teamId eq id).first()

    private suspend fun saveBothFeatures(response: BuildGameFeaturesResponse) {
        saveFeatures(response.gameId, response.home)
        saveFeatures(response.gameId, response.away)
    }

    private suspend fun saveFeatures(gameId: Int, features: TeamFeatures) {
        db.getCollection<NbaFeatures>().save(
            NbaFeatures(
                gameId,
                features.teamId,
                features.shooting,
                features.turnovers,
                features.rebounding,
                features.freebies,
                features.backToBack,
                features.bestSpread,
                features.result
            )
        )
    }
}
