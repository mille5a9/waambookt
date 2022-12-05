package waambokt.commands

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.User
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import mu.KotlinLogging

class Reprimand private constructor(
    private val event: ChatInputCommandInteractionCreateEvent? = null,
    private var offender: User? = null,
    private var reason: String = ""
) : Command() {

    init {
        logger.info("init ping")
        if (event != null) {
            offender = event.interaction.command.users["offender"]!!
            reason = event.interaction.command.strings["reason"] ?: ""
        }
    }

    override suspend fun respond() {
        logger.info("respond reprimand")
        val response = event!!.interaction.deferPublicResponse()
        response.respond {
            this.content = execute()
        }
    }

    override suspend fun execute(): String {
        logger.info { "offender is $offender" }
        logger.info { "reason is $reason" }
        return reason
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        suspend operator fun invoke(event: ChatInputCommandInteractionCreateEvent) {
            logger.info("invoked reprimand")
            Reprimand(event).respond()
        }
        suspend operator fun invoke(offender: User, reason: String): String {
            logger.info("invoked reprimand test")
            return Reprimand(offender = offender, reason = reason).execute()
        }
    }
}
