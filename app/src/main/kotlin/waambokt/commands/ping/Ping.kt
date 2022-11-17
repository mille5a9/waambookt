package waambokt.commands.ping

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import mu.KotlinLogging
import waambokt.commands.ICommand

object Ping : ICommand<ChatInputCommandInteractionCreateEvent, Void> {
    private val logger = KotlinLogging.logger {}

    override suspend fun invoke(
        event: ChatInputCommandInteractionCreateEvent
    ) {
        logger.info("invoked ping")
        val response = event.interaction.deferEphemeralResponse()
        response.respond {
            this.content = listOf<Void>().execute()
        }
    }

    // command logic is split from event processing;
    // this is for keeping the logic unit-testable when
    // the events and responses cannot be mocked
    override suspend fun List<Void>.execute(): String = "Pong!"
}
