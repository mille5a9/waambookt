package org.waambokt.service.waambokt.configs

import dev.kord.rest.json.request.ApplicationCommandCreateRequest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.waambokt.service.waambokt.enums.CommandEnum

object CommandsConfig {
    private val logger = KotlinLogging.logger {}

    private val filenames = CommandEnum.values().map { "${it.cmdName}.json" }

    fun loadCommands(path: String): List<ApplicationCommandCreateRequest> {
        logger.info("loading commands")
        return filenames.map {
            val json = javaClass.classLoader.getResource(path + it)?.readText() ?: ""
            return@map Json.decodeFromString<ApplicationCommandCreateRequest>(json)
        }
    }
}
