package org.waambokt.service.waambokt.extensions

import dev.kord.core.Kord
import dev.kord.rest.json.request.ApplicationCommandCreateRequest
import org.waambokt.common.constants.Environment
import org.waambokt.common.extensions.EnvironmentExtension.bool
import org.waambokt.common.extensions.EnvironmentExtension.sf

object KordExtension {
    suspend fun Kord.deleteAllApplicationCommands(envars: Environment) {
        this.rest.interaction
            .getGuildApplicationCommands(this.selfId, envars.sf("TESTGUILD"), withLocalizations = false)
            .forEach {
                this.rest.interaction.deleteGuildApplicationCommand(
                    this.selfId,
                    envars.sf("TESTGUILD"),
                    it.id
                )
            }
        if (envars.bool("ISPROD")) {
            this.rest.interaction
                .getGuildApplicationCommands(this.selfId, envars.sf("PRODGUILD"), withLocalizations = false)
                .forEach {
                    this.rest.interaction.deleteGuildApplicationCommand(
                        this.selfId,
                        envars.sf("PRODGUILD"),
                        it.id
                    )
                }
        }
    }

    suspend fun Kord.createAllApplicationCommands(
        envars: Environment,
        commands: List<ApplicationCommandCreateRequest>
    ) {
        this.rest.interaction.createGuildApplicationCommands(this.selfId, envars.sf("TESTGUILD"), commands)

        if (envars.bool("ISPROD")) {
            this.rest.interaction.createGuildApplicationCommands(
                this.selfId,
                envars.sf("PRODGUILD"),
                commands
            )
        }
    }
}
