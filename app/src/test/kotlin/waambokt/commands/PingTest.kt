package waambokt.commands

import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PingTest {
    private val expectedPing = "Pong!"
    private val expectedPong = "Ping!"

    @RelaxedMockK private lateinit var event: ChatInputCommandInteractionCreateEvent

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `execute ping happy path`() = runBlocking {
        Assertions.assertEquals(expectedPing, Ping(event, "ping").execute())
    }

    @Test
    fun `execute pong happy path`() = runBlocking {
        Assertions.assertEquals(expectedPong, Ping(event, "pong").execute())
    }
}
