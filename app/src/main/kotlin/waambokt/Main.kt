/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package waambokt

import dev.kord.core.Kord
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

fun main() = runBlocking {
    run()
}

suspend fun run() {

    val kord = Kord(System.getenv("TOKEN"))
    val pingPong = ReactionEmoji.Unicode("\uD83C\uDFD3")

    kord.on<MessageCreateEvent> {
        if (message.content != "!ping") return@on

        val response = message.channel.createMessage("Pong!")
        response.addReaction(pingPong)

        delay(5000)
        message.delete()
        response.delete()
    }

    kord.login {
        @OptIn(PrivilegedIntent::class)
        intents += Intent.MessageContent
    }
}

private fun getToken(): String {
    var prop = Properties()
    // set specific absolute path if in root (docker container)
    if (System.getProperty("user.dir") == "/")
        return loadProp(File("/app/.env.properties"))
    else
        return loadProp(File("src/main/resources/env.properties"))
}

private fun loadProp(env: File): String {
    val prop = Properties()
    FileInputStream(env).use { prop.load(it) }
    return prop.getProperty("token")
}
