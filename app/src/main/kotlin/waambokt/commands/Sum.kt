package waambokt.commands

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import mu.KotlinLogging

class Sum
private constructor(
    private val event: ChatInputCommandInteractionCreateEvent,
    private var first: Long,
    private var second: Long
) : Command() {

    override suspend fun respond() {
        logger.info("respond sum")
        val response = event.interaction.deferPublicResponse()
        response.respond { this.content = execute() }
    }

    override suspend fun execute(): String {
        logger.info("executing sum")
        return "$first + $second = ${first + second}"
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        suspend operator fun invoke(
            event: ChatInputCommandInteractionCreateEvent,
            first: Long? = null,
            second: Long? = null
        ): Sum {
            logger.info("building Sum")
            return Sum(
                event,
                (first ?: event.interaction.command.integers["first"]!!),
                (second ?: event.interaction.command.integers["second"]!!)
            )
        }
    }
}
