package org.waambokt.service.cron

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.waambokt.common.constants.Env
import org.waambokt.common.constants.Environment

fun main() {
    val logger = KotlinLogging.logger {}
    val envars = Environment(
        Env.TOKEN,
        Env.PRODGUILD,
        Env.TESTGUILD,
        Env.ISPROD,
        Env.CLEARCOMMANDS,
        Env.PORT,
        Env.ODDS,
        Env.MONGO_CONNECTION_STRING,
        Env.GRPC
    )
    val scheduler = CronService()
    scheduler.start(5)
    runBlocking { scheduler.join() }
}
