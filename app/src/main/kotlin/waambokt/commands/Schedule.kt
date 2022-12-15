package waambokt.commands

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import mu.KotlinLogging
import org.json.JSONArray
import org.json.JSONObject
import waambokt.constants.Leagues
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.NoSuchElementException

class Schedule
private constructor(
    private val event: ChatInputCommandInteractionCreateEvent,
    private val leagueName: String
) : Command() {
    private val espnApiBaseUrl: String = "https://site.web.api.espn.com/apis/v2/scoreboard/header"

    override suspend fun respond() {
        logger.info("respond schedule")
        val response = event.interaction.deferEphemeralResponse()
        response.respond {
            this.content = execute()
            this.embeds = embeds().toMutableList()
        }
    }

    override suspend fun execute(): String {
        val league = Leagues.aliases[leagueName.lowercase()] ?: return "Pick a real sport"
        Leagues.sportNames[league.lowercase()] ?: return "Sport not found for given league"
        return ""
    }

    private suspend fun embeds(): List<EmbedBuilder> {
        logger.info("executing schedule")

        val league = Leagues.aliases[leagueName.lowercase()] ?: throw NoSuchElementException()
        val sport = Leagues.sportNames[league.lowercase()] ?: throw NoSuchElementException()

        val dateUrlParam = addDateParam(sport)

        logger.info("$espnApiBaseUrl?sport=$sport&league=$league$dateUrlParam")
        val response = HttpClient().get("$espnApiBaseUrl?sport=$sport&league=$league$dateUrlParam")

        val json = JSONObject(response.body<String>()).gJA("sports").gJO(0).gJA("leagues").gJO(0)
        val events = json.gJA("events")

        // build event info
        val eventList = events.toEventList()

        // get unique days and build an embed per day with info about that day of games
        return eventList.distinctDates().formatDates().mapDatesFromEvents(eventList)
    }

    private fun List<Event>.distinctDates() = this.distinctBy { it.date.dayOfYear }

    private fun List<Event>.formatDates() = this.map {
        it.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)) ?: "Date not found"
    }

    private fun List<String>.mapDatesFromEvents(events: List<Event>) = this.mapIndexedNotNull { i, it ->
        if (i > 9) null
        else {
            val daysEvents = events.filter { x -> x.date.formatDateLong() == it }
            EmbedBuilder().build(it, daysEvents.makeOut())
        }
    }

    private fun List<Event>.makeOut() =
        this.joinToString("\n", "```", "```", 16, "...") {
            it.name + ": " + it.summary + if (it.broadcast != null) " (on " + it.broadcast + ")" else ""
        }

    private fun EmbedBuilder.build(
        title: String? = null,
        description: String? = null
    ): EmbedBuilder {
        this.title = title
        this.description = description
        return this
    }

    private fun ZonedDateTime.formatDateLong() = this.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))

    private fun addDateParam(sport: String) = if (sport == "basketball" || sport == "hockey") {
        "&dates=" + Date().toInstant().atOffset(ZoneOffset.UTC).d8FormatEst() + "-" +
            Date().toInstant().atOffset(ZoneOffset.UTC).plusDays(2).d8FormatEst()
    } else ""

    private fun OffsetDateTime.d8FormatEst() =
        this.format(DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.ofHours(-5)))

    private fun JSONArray.toEventList() = this.mapIndexed { i, _ ->
        val event = this.gJO(i)
        Event(
            event.getString("shortName"),
            ZonedDateTime.parse(event.getString("date")).minusHours(5),
            event.getString("summary"),
            event.optJSONObject("broadcasts")?.optString("broadcast")
        )
    }

    private data class Event(
        val name: String,
        val date: ZonedDateTime,
        val summary: String,
        val broadcast: String?
    )

    private fun JSONObject.gJA(key: String) = this.getJSONArray(key)

    private fun JSONArray.gJO(index: Int) = this.getJSONObject(index)

    companion object {
        private val logger = KotlinLogging.logger {}

        operator fun invoke(
            event: ChatInputCommandInteractionCreateEvent,
            league: String? = null
        ): Schedule {
            logger.info("building Schedule")
            return Schedule(
                event,
                (league ?: event.interaction.command.strings["league"] ?: "")
            )
        }
    }
}
