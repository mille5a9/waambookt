package waambokt.extensions

import dev.kord.core.Kord
import waambokt.config.Env

object KordExtension {
    suspend fun Kord.deleteAllApplicationCommands() {
        this.rest.interaction.getGuildApplicationCommands(
            this.selfId,
            Env.testGuild,
            withLocalizations = false
        ).forEach {
            this.rest.interaction.deleteGuildApplicationCommand(
                this.selfId,
                Env.testGuild,
                it.id
            )
        }
        if (Env.isProd) {
            this.rest.interaction.getGuildApplicationCommands(
                this.selfId,
                Env.prodGuild,
                withLocalizations = false
            ).forEach {
                this.rest.interaction.deleteGuildApplicationCommand(
                    this.selfId,
                    Env.prodGuild,
                    it.id
                )
            }
        }
    }
}
