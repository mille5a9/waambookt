package waambokt.extensions

import waambokt.constants.Nba
import waambokt.constants.Nba.HowToBetEnum
import waambokt.models.NbaMatchup

object NbaMatchupExtension {
    fun NbaMatchup.net3Minus2(): HowToBetEnum {
        val implSpreadH = this.awayNet - this.homeNet - 3
        if (implSpreadH < (this.homeSpread - Nba.min)) {
            return HowToBetEnum.HOME_SPREAD
        } else if (implSpreadH > (this.homeSpread + Nba.min)) {
            return HowToBetEnum.AWAY_SPREAD
        }
        return HowToBetEnum.NO_CONTEST
    }
}
