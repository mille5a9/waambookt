package waambokt.commands.ping

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import waambokt.commands.ping.Ping.execute

class PingTest {

    private val expected = "Pong!"

    @Test
    fun `execute ping happy path`() = runBlocking {
        val response = listOf<Void>().execute()

        Assert.assertEquals(expected, response)
    }
}
