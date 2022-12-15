/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package waambokt

import dev.kord.core.Kord
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import waambokt.commands.* // ktlint-disable no-wildcard-imports
import waambokt.config.Database
import waambokt.config.Env
import waambokt.config.Registry
import waambokt.extensions.KordExtension.createAllApplicationCommands
import waambokt.extensions.KordExtension.deleteAllApplicationCommands

fun main(): Unit = runBlocking {
    val logger = KotlinLogging.logger {}

    logger.info("discord client login...")
    val kord = Kord(Env.token)

    // remove chat input commands if ENV bool is set
    if (Env.clearCommands) {
        kord.deleteAllApplicationCommands()
    }

    val commands = Registry.loadCommands("commands/")

    // There are no global commands, because discord takes a while to register them
    // and being able to DM the bot is not worth the hassle during development
    kord.createAllApplicationCommands(commands)

    // init database
    val db = Database.getDb()

    kord.on<GuildChatInputCommandInteractionCreateEvent> {
        logger.info {
            "GuildChatInputCommandInteractionCreateEvent ${this.interaction.invokedCommandName}"
        }
        when (this.interaction.invokedCommandName) {
            "ping" -> Ping(this).respond()
            "sum" -> Sum(this).respond()
            "reprimand" -> Reprimand(db, this).respond()
            "net" -> Net(db, this).respond()
            "schedule" -> Schedule(this).respond()
        }
    }

    kord.login {
        @OptIn(PrivilegedIntent::class)
        intents += Intent.MessageContent
        intents += Intent.GuildIntegrations
    }
}
