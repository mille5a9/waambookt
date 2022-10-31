package waambokt.commands.ping

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import kotlinx.coroutines.reactor.awaitSingleOrNull
import mu.KotlinLogging
import waambokt.commands.ICommand

object Ping : ICommand<ChatInputInteractionEvent, Void> {
    private val logger = KotlinLogging.logger {}

    override suspend fun invoke(
        event: ChatInputInteractionEvent
    ): Void? {
        logger.info("invoked ping")
        return event.reply()
            .withEphemeral(true)
            .withContent(listOf<Void>().execute())
            .awaitSingleOrNull()
    }

    // command logic is split from event processing;
    // this is for keeping the logic unit-testable when
    // the D4J events and responses cannot be mocked
    override suspend fun List<Void>.execute(): String = "Pong!"
}
