package waambokt.commands

import com.mongodb.reactivestreams.client.MongoDatabase
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.User
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import mu.KotlinLogging
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import waambokt.models.ReprimandLog
import waambokt.repos.MongoRepo.getOne
import waambokt.repos.MongoRepo.overwriteOneById
import waambokt.repos.MongoRepo.pushOne

class Reprimand private constructor(
    private val mongo: CoroutineDatabase,
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
        logger.info { "offender is ${offender.username}" }
        logger.info { "reason is $reason" }
        val reprimandLogs = mongo.getCollection<ReprimandLog>("reprimandLog")

        val existingLog = reprimandLogs.getOne(
            ReprimandLog::userId eq offender.id.value.toString()
        )

        // brand new, never-before-reprimanded user
        if (existingLog == null) reprimandLogs.pushOne(
            ReprimandLog(
                offender.id.value.toString(),
                reasons = if (reason.isNotEmpty()) listOf(reason) else emptyList()
            )
        )
        // previously reprimanded user, add reason to list and increment count
        else reprimandLogs.overwriteOneById(
            existingLog._id,
            ReprimandLog(
                existingLog.userId,
                existingLog.count.plus(1),
                existingLog.reasons.plusNotEmpty(reason)
            )
        )

        return "${offender.mention} has been reprimanded" + if (reason != "") ": $reason" else ""
    }

    private fun List<String>.plusNotEmpty(item: String): List<String> {
        return if (item.isNotEmpty()) this.plus(reason) else this
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        fun build(
            db: MongoDatabase,
            event: ChatInputCommandInteractionCreateEvent,
            offender: User? = null,
            reason: String? = null
        ): Reprimand {
            logger.info("building reprimand")
            return Reprimand(
                CoroutineDatabase(db),
                event,
                (offender ?: event.interaction.command.users["offender"])!!,
                (reason ?: event.interaction.command.strings["reason"])!!
            )
        }
    }
}
