package org.waambokt.service.odds.extensions

import org.json.JSONArray
import org.waambokt.service.odds.models.Bookmaker
import org.waambokt.service.odds.models.Event
import org.waambokt.service.odds.models.Market
import org.waambokt.service.odds.models.Outcome

object JSONArrayExtension {
    fun JSONArray.mapEvents() = List(this.length()) {
        this.getJSONObject(it).let { obj ->
            Event(
                obj.getString("id"),
                obj.getString("commence_time"),
                obj.getString("home_team"),
                obj.getString("away_team"),
                obj.getJSONArray("bookmakers").mapBookmakers()
            )
        }
    }

    private fun JSONArray.mapBookmakers() = List(this.length()) {
        this.getJSONObject(it).let { obj ->
            Bookmaker(
                obj.getString("key"),
                obj.getString("title"),
                obj.getJSONArray("markets").mapMarkets()
            )
        }
    }

    private fun JSONArray.mapMarkets() = List(this.length()) {
        this.getJSONObject(it).let { obj ->
            Market(
                obj.getString("key"),
                obj.getJSONArray("outcomes").mapOutcomes()
            )
        }
    }

    private fun JSONArray.mapOutcomes() = List(this.length()) {
        this.getJSONObject(it).let { obj ->
            Outcome(
                obj.getString("name"),
                obj.getDouble("price"),
                obj.optDouble("point")
            )
        }
    }
}
