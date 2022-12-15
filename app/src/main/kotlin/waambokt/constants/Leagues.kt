package waambokt.constants

object Leagues {
    // store possible league aliases as keys and the corresponding espn URL name for that league as the value
    val aliases = mapOf(
        // college basketball
        "mens-college-basketball" to "mens-college-basketball",
        "cbb" to "mens-college-basketball",
        "mbb" to "mens-college-basketball",
        "college basketball" to "mens-college-basketball",
        "college-basketball" to "mens-college-basketball",
        "college hoops" to "mens-college-basketball",
        "college-hoops" to "mens-college-basketball",
        "ncaam" to "mens-college-basketball",
        "ncaa m" to "mens-college-basketball",
        "ncaa-m" to "mens-college-basketball",
        "ncaabb" to "mens-college-basketball",
        "ncaa bb" to "mens-college-basketball",
        "ncaa-bb" to "mens-college-basketball",
        "ncaa basketball" to "mens-college-basketball",
        "ncaa-basketball" to "mens-college-basketball",
        "men\'s basketball" to "mens-college-basketball",
        "mens basketball" to "mens-college-basketball",
        "amateurism" to "mens-college-basketball",

        // nba
        "nba" to "nba",
        "aba" to "nba",
        "national basketball association" to "nba",
        "association" to "nba",
        "the association" to "nba",
        "pro hoops" to "nba",
        "pro basketball" to "nba",
        "basketball" to "nba",

        // college football
        "college-football" to "college-football",
        "cfb" to "college-football",
        "college football" to "college-football",
        "collegiate football" to "college-football",
        "college fb" to "college-football",
        "college-fb" to "college-football",
        "collegiate fb" to "college-football",
        "ncaaf" to "college-football",
        "ncaafb" to "college-football",
        "ncaa fb" to "college-football",
        "ncaa football" to "college-football",
        "boy\'s league" to "college-football",
        "boys league" to "college-football",

        // nfl
        "nfl" to "nfl",
        "national football league" to "nfl",
        "the league" to "nfl",
        "men\"s league" to "nfl",
        "mens league" to "nfl",
        "tnf" to "nfl",
        "snf" to "nfl",
        "mnf" to "nfl",
        "redzone" to "nfl",
        "lombardi" to "nfl",
        "football" to "nfl",

        // nhl
        "nhl" to "nhl",
        "national hockey league" to "nhl",
        "hockey" to "nhl",
        "hockey league" to "nhl",
        "hockey-league" to "nhl",
        "the show" to "nhl",

        // mlb
        "mlb" to "mlb",
        "baseball" to "mlb",
        "major league baseball" to "mlb",
        "ballpark" to "mlb",
        "ball park" to "mlb",

        // ncaaw
        "womens-college-basketball" to "womens-college-basketball",
        "ncaaw" to "womens-college-basketball",
        "womens basketball" to "womens-college-basketball",
        "women\'s basketball" to "womens-college-basketball",

        // wnba
        "wnba" to "wnba",
        "womens nba" to "wnba",
        "women\'s nba" to "wnba",
        "lady hoops" to "wnba",

        // ufc
        "ufc" to "ufc",
        "mma" to "ufc",
        "fight" to "ufc",
        "fights" to "ufc",
        "ppv" to "ufc",
        "ultimate fighting championship" to "ufc",

        // wta
        "wta" to "wta",
        "tennis" to "wta"
    )

    val sportNames = mapOf(
        "college-football" to "football",
        "nfl" to "football",
        "nba" to "basketball",
        "wnba" to "basketball",
        "mens-college-basketball" to "basketball",
        "womens-college-basketball" to "basketball",
        "nhl" to "hockey",
        "mlb" to "baseball",
        "ufc" to "mma",
        "wta" to "tennis"
    )
}
