package waambokt.commands

import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SumTest {

    private val first: Long = 4
    private val second: Long = 5
    private val sum: Long = 9
    private val expected = "$first + $second = $sum"

    @RelaxedMockK private lateinit var event: ChatInputCommandInteractionCreateEvent

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `execute Sum happy path`() = runBlocking {
        Assertions.assertEquals(expected, Sum(event, first, second).execute())
    }
}
