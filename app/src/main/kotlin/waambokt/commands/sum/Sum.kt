package waambokt.commands.sum

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import mu.KotlinLogging
import waambokt.commands.ICommand

object Sum : ICommand<ChatInputCommandInteractionCreateEvent, Long> {
    private val logger = KotlinLogging.logger {}

    override suspend fun invoke(
        event: ChatInputCommandInteractionCreateEvent
    ) {
        logger.info("invoked sum")
        val response = event.interaction.deferPublicResponse()
        val first: Long = event.interaction.command.integers["first"] ?: 0
        val second: Long = event.interaction.command.integers["second"] ?: 0
        response.respond {
            this.content = listOf(first, second).execute()
        }
    }

    override suspend fun List<Long>.execute(): String {
        logger.info("executing sum")
        return "${this[0]} + ${this[1]} = ${this[0] + this[1]}"
    }
}
