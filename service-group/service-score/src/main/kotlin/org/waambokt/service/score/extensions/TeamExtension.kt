package org.waambokt.service.score.extensions

import org.waambokt.service.spec.score.GameResult.Team

object TeamExtension {
    fun Team.shooting() = (this.fg + (0.5 * this.fg3P)) / this.fga

    fun Team.turnovers() = (this.turnovers / (this.fga + (0.44 * this.fta) + this.turnovers))

    fun Team.rebounding(opp: Team) =
        (this.oreb.toDouble() / (this.oreb + opp.dreb)) + (this.dreb / (opp.oreb + this.dreb))

    fun Team.freebies() = (this.ft.toDouble() / this.fga)
}
