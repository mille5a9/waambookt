package org.waambokt.service.waambokt

import dev.kord.core.Kord
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.kordLogger
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.waambokt.common.constants.Env
import org.waambokt.common.constants.Environment
import org.waambokt.common.extensions.EnvironmentExtension.bool
import org.waambokt.service.waambokt.commands.Ping
import org.waambokt.service.waambokt.commands.Sum
import org.waambokt.service.waambokt.configs.CommandsConfig
import org.waambokt.service.waambokt.enums.CommandEnum
import org.waambokt.service.waambokt.extensions.KordExtension.createAllApplicationCommands
import org.waambokt.service.waambokt.extensions.KordExtension.deleteAllApplicationCommands

fun main(): Unit = runBlocking {
    val logger = KotlinLogging.logger {}
    val envars = Environment(
        Env.TOKEN,
        Env.PRODGUILD,
        Env.TESTGUILD,
        Env.ISPROD,
        Env.CLEARCOMMANDS,
        Env.PORT
    )

    logger.info("discord client login...")
    val kord = Kord(envars["TOKEN"])

    // remove chat input commands if ENV bool is set
    if (envars.bool("CLEARCOMMANDS")) {
        kord.deleteAllApplicationCommands(envars)
    }

    val commands = CommandsConfig.loadCommands("commands/")

    // There are no global commands, because discord takes a while to register them
    // and being able to DM the bot is not worth the hassle during development
    kord.createAllApplicationCommands(envars, commands)

    kord.on<GuildChatInputCommandInteractionCreateEvent> {
        logger.info {
            "GuildChatInputCommandInteractionCreateEvent ${this.interaction.invokedCommandName}"
        }
        this.digest()
    }

    kord.login {
        @OptIn(PrivilegedIntent::class)
        intents += Intent.MessageContent
        intents += Intent.GuildIntegrations
    }
}

suspend fun GuildChatInputCommandInteractionCreateEvent.digest() {
    return when (CommandEnum.values().find { it.cmdName == this.interaction.invokedCommandName }) {
        CommandEnum.PING -> Ping(this).respond()
        CommandEnum.SUM -> Sum(this).respond()
        else -> kordLogger.info { "Command ${this.interaction.invokedCommandName} not found. Skipping..." }
    }
}
