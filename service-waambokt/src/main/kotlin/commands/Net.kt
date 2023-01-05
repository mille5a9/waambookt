package org.waambokt.service.waambokt.commands

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import mu.KotlinLogging
import org.waambokt.service.spec.net.FormulaRequest
import org.waambokt.service.spec.net.FormulaResult
import org.waambokt.service.spec.net.NetServiceGrpcKt
import kotlin.math.roundToInt

class Net
private constructor(
    private val netService: NetServiceGrpcKt.NetServiceCoroutineStub,
    private val event: ChatInputCommandInteractionCreateEvent,
    private val formula: Int,
    private val hideNoContests: Boolean
) : Command() {
    override suspend fun respond() {
        logger.info("respond net")
        val response = event.interaction.deferEphemeralResponse()
        response.respond { this.content = execute() }
    }

    override suspend fun execute(): String {
        logger.info("execute net")
        val bestBets = netService.getFormulation(FormulaRequest.newBuilder().setFormulaValue(formula).build())

        return bestBets.formulaResultsList
            .filter { !hideNoContests || it.choice != FormulaResult.FormulaChoiceEnum.NO_CONTEST }
            .joinToString("\n", "```", "```") {
                if (it.book.isBlank()) "Don't bet on the ${it.name} game"
                else "${it.name.split(' ').last()} ${it.line} on ${it.book} at ${it.odds.toAmericanOdds()}"
            }
    }

    private fun Double.toAmericanOdds() = (if (this >= 2) (this - 1) * 100 else (-100) / (this - 1)).roundToInt()

    companion object {
        private val logger = KotlinLogging.logger {}

        operator fun invoke(
            netService: NetServiceGrpcKt.NetServiceCoroutineStub,
            event: ChatInputCommandInteractionCreateEvent,
            formula: Int? = null,
            hideNoContests: Boolean? = null
        ): Net {
            logger.info("building net")
            return Net(
                netService,
                event,
                formula ?: event.interaction.command.integers["formula"]?.toInt() ?: 0,
                hideNoContests ?: event.interaction.command.booleans["hide_no_contests"] ?: true
            )
        }
    }
}
