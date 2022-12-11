package waambokt.commands

import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PingTest {
    private val expected = "Pong!"

    @RelaxedMockK private lateinit var event: ChatInputCommandInteractionCreateEvent

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `execute ping happy path`() = runBlocking {
        Assertions.assertEquals(expected, Ping(event).execute())
    }
}
