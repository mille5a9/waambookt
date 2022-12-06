package waambokt.commands

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.User
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import mu.KotlinLogging
import org.litote.kmongo.eq
import waambokt.config.Database
import waambokt.data.ReprimandLog

class Reprimand private constructor(
    private val event: ChatInputCommandInteractionCreateEvent,
    private val offender: User,
    private val reason: String
) : Command() {

    override suspend fun respond() {
        logger.info("respond reprimand")
        val response = event.interaction.deferPublicResponse()
        response.respond {
            this.content = execute()
        }
    }

    override suspend fun execute(): String {
        logger.info { "offender is $offender" }
        logger.info { "reason is $reason" }
        val reprimandLogs = Database.getDb().getCollection<ReprimandLog>()

        val existingLog = reprimandLogs.findOne(ReprimandLog::userId eq offender.id.value)

        // brand new, never-before-reprimanded user
        if (existingLog == null) {
            reprimandLogs.insertOne(ReprimandLog(offender.id.value, 1, listOf(reason)))
        }
        // previously reprimanded user, add reason to list and increment count
        else {
            val updatedLog = ReprimandLog(
                existingLog.userId,
                existingLog.count + 1,
                existingLog.reasons.plus(reason)
            )
            reprimandLogs.save(updatedLog)
        }
        return "${offender.mention} has been reprimanded" + if (reason != "") ": $reason" else ""
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        fun build(
            event: ChatInputCommandInteractionCreateEvent,
            offender: User? = null,
            reason: String? = null
        ): Reprimand {
            logger.info("building reprimand")
            return Reprimand(
                event,
                (offender ?: event.interaction.command.users["offender"])!!,
                (reason ?: event.interaction.command.strings["reason"])!!
            )
        }
    }
}
