package org.waambokt.common.constants

object Nba {
    const val min: Double = 2.0

    enum class NetResultsEnum {
        INDETERMINATE,
        HOME_WIN,
        AWAY_WIN,
        DRAW
    }

    enum class HowToBetEnum {
        HOME_SPREAD,
        AWAY_SPREAD,
        NO_CONTEST
    }

    val abbr: Map<String, String> = mapOf(
        "ATL" to "Hawks",
        "BKN" to "Nets",
        "BOS" to "Celtics",
        "CHA" to "Hornets",
        "CHI" to "Bulls",
        "CLE" to "Cavaliers",
        "DAL" to "Mavericks",
        "DEN" to "Nuggets",
        "DET" to "Pistons",
        "GS" to "Warriors",
        "HOU" to "Rockets",
        "IND" to "Pacers",
        "LAC" to "Clippers",
        "LAL" to "Lakers",
        "MEM" to "Grizzlies",
        "MIA" to "Heat",
        "MIL" to "Bucks",
        "MIN" to "Timberwolves",
        "NO" to "Pelicans",
        "NY" to "Knicks",
        "OKC" to "Thunder",
        "ORL" to "Magic",
        "PHI" to "76ers",
        "PHX" to "Suns",
        "POR" to "Blazers",
        "SAC" to "Kings",
        "SA" to "Spurs",
        "TOR" to "Raptors",
        "UTAH" to "Jazz",
        "WSH" to "Wizards"
    )
}
