package waambokt.config

import dev.kord.common.entity.Snowflake

object Env {
    val testGuild = Snowflake(System.getenv("TESTGUILD"))
    val prodGuild = Snowflake(System.getenv("PRODGUILD"))
    val clearCommands = System.getenv("CLEARCOMMANDS").toBoolean()
    val isProd = System.getenv("PROD").toBoolean()
    val token: String = System.getenv("TOKEN")
}
