package waambokt.commands

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import mu.KotlinLogging

class Sum private constructor(
    private val event: ChatInputCommandInteractionCreateEvent? = null,
    private var first: Long = 0,
    private var second: Long = 0
) : Command() {

    init {
        logger.info("init sum")
        if (event != null) {
            first = event.interaction.command.integers["first"]!!
            second = event.interaction.command.integers["second"]!!
        }
    }

    override suspend fun respond() {
        logger.info("respond sum")
        val response = event!!.interaction.deferPublicResponse()
        response.respond {
            this.content = execute()
        }
    }

    override suspend fun execute(): String {
        logger.info("executing sum")
        return "$first + $second = ${first + second}"
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        suspend operator fun invoke(event: ChatInputCommandInteractionCreateEvent) {
            logger.info("invoked sum")
            Sum(event).respond()
        }

        suspend operator fun invoke(first: Long, second: Long): String {
            logger.info("invoked sum test")
            return Sum(first = first, second = second).execute()
        }
    }
}
