package waambokt.commands

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import mu.KotlinLogging

class Ping private constructor(
    private val event: ChatInputCommandInteractionCreateEvent
) : Command() {

    // init contains command argument assignment logic
    init {
        logger.info("init ping")
    }

    // entry point for the invoke operator after initialization.
    // builds the response
    override suspend fun respond() {
        logger.info("respond ping")
        val response = event.interaction.deferEphemeralResponse()
        response.respond {
            this.content = execute()
        }
    }

    // command logic is split from event processing;
    // this is for keeping the logic unit-testable when
    // the events and responses cannot be mocked
    override suspend fun execute(): String = "Pong!"

    companion object {
        private val logger = KotlinLogging.logger {}

        suspend operator fun invoke(
            event: ChatInputCommandInteractionCreateEvent
        ): Ping {
            logger.info("building ping")
            return Ping(
                event
            )
        }
    }
}
