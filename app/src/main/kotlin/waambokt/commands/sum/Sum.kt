package waambokt.commands.sum

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import kotlinx.coroutines.reactor.awaitSingleOrNull
import mu.KotlinLogging
import waambokt.commands.ICommand

object Sum : ICommand<ChatInputInteractionEvent, Long> {
    private val logger = KotlinLogging.logger {}

    override suspend fun invoke(
        event: ChatInputInteractionEvent
    ): Void? {
        logger.info("invoked sum")
        val first: Long = event.getOption("first")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .get()
        val second: Long = event.getOption("second")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .get()
        return event.reply()
            .withContent(listOf(first, second).execute())
            .awaitSingleOrNull()
    }

    override suspend fun List<Long>.execute(): String {
        logger.info("executing sum")
        return "${this[0]} + ${this[1]} = ${this[0] + this[1]}"
    }
}
