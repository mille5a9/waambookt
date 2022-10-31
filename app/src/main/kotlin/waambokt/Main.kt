/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package waambokt

import discord4j.common.JacksonResources
import discord4j.core.DiscordClient
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import waambokt.commands.ping.Ping
import waambokt.commands.sum.Sum
import waambokt.config.Registry

fun main(): Unit = runBlocking {
    val logger = KotlinLogging.logger {}

    logger.info("discord client login...")
    val client = DiscordClient.create(System.getenv("TOKEN"))
    client.login().block()

    logger.info("init commands...")
    val d4jMapper = JacksonResources.create()
    val commands: List<ApplicationCommandRequest> = Registry.filenames.map {
        val json = javaClass.classLoader.getResource("commands/$it")?.readText()
        d4jMapper.objectMapper.readValue(json, ApplicationCommandRequest::class.java)
    }

    // There are no global commands, because discord takes a while to register them
    // and being able to DM the bot is not worth the hassle during development
    client.applicationService.bulkOverwriteGuildApplicationCommand(
        client.applicationId.block() ?: 0,
        System.getenv("TESTGUILD").toLong(), // waambokt test server id
        commands
    ).subscribe()

    // test bots are not part of the prod discord server
    if (System.getenv("PROD").toBoolean()) {
        client.applicationService.bulkOverwriteGuildApplicationCommand(
            client.applicationId.block() ?: 0,
            System.getenv("PRODGUILD").toLong(), // waambot prod server id
            commands
        ).subscribe()
    }

    logger.info("init ChatInputInteractionEvent listener...")
    client.withGateway {
        mono {
            it.on(ChatInputInteractionEvent::class.java)
                .asFlow()
                .collect {
                    logger.info("received ChatInputInteractionEvent")
                    it.deferReply()

                    logger.debug("when ${it.commandName}")
                    when (it.commandName) {
                        "ping" -> Ping.invoke(it)
                        "sum" -> Sum.invoke(it)
                    }
                }
        }
    }.block()
}
