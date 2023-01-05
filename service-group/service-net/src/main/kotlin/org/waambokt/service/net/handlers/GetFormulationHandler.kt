package org.waambokt.service.net.handlers

import mu.KotlinLogging
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.waambokt.common.constants.Nba
import org.waambokt.common.extensions.TimestampExtension.getDays
import org.waambokt.service.net.helpers.RefreshNetsHelper
import org.waambokt.service.net.helpers.RefreshNetsHelper.findTeamNet
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
import java.time.LocalDate

class GetFormulationHandler(
    private val db: CoroutineDatabase,
    private val oddsService: OddsServiceGrpcKt.OddsServiceCoroutineStub
) {
    suspend fun handle(request: FormulaRequest): FormulaResponse {
        return when (request.formula) {
            FormulaRequest.FormulasEnum.NET_3MINUS_2 -> net3minus2()
            else -> FormulaResponse.getDefaultInstance()
        }
    }

    private suspend fun net3minus2(): FormulaResponse {
        val spreads = getBestSpreads()
        val nets = RefreshNetsHelper.refreshNet(db)
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
                    .setGameId(it.gameId.toString())
                    .setName(side.first)
                    .setLine(side.second.bestLine)
                    .setOdds(side.second.bestOdds)
                    .setBook(side.second.bestBook)
                    .build()
            }
        ).build()
    }

    private fun NbaOdds.makeChoice(nets: List<NbaNet>): FormulaChoiceEnum {
        val homeNet = nets.findTeamNet(homeTeamName)
        val awayNet = nets.findTeamNet(awayTeamName)
        val homeSpreadDiff = (homeOrOver.bestLine + Nba.min) - (awayNet - homeNet - 3)
        val awaySpreadDiff = (awayOrUnder.bestLine + Nba.min) - (3 + homeNet - awayNet)
        return if (homeSpreadDiff > 0 && awaySpreadDiff > 0) FormulaChoiceEnum.NO_CONTEST
        else if (homeSpreadDiff > awaySpreadDiff) FormulaChoiceEnum.HOME_SPREAD
        else FormulaChoiceEnum.AWAY_SPREAD
    }

    private suspend fun getBestSpreads(): NbaOddsResponse {
        return oddsService.getNbaOdds(
            NbaOddsRequest.newBuilder()
                .addOddsMarkets(NbaOddsRequest.NbaOddsMarketsEnum.SPREADS)
                .build()
        )
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
