package waambokt.commands

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import waambokt.commands.Ping

class PingTest {
    private val expected = "Pong!"

    @Test
    fun `execute ping happy path`() = runBlocking {
        Assertions.assertEquals(expected, Ping())
    }
}
