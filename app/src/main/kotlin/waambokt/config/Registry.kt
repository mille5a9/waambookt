package waambokt.config

import dev.kord.rest.json.request.ApplicationCommandCreateRequest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

object Registry {
    private val logger = KotlinLogging.logger {}

    private val filenames = listOf(
        "ping.json",
        "sum.json"
    )

    fun loadCommands(
        path: String
    ): List<ApplicationCommandCreateRequest> {
        logger.info("loading commands")
        return filenames.map {
            val json = javaClass.classLoader.getResource(path + it)?.readText() ?: ""
            return@map Json.decodeFromString<ApplicationCommandCreateRequest>(json)
        }
    }
}
