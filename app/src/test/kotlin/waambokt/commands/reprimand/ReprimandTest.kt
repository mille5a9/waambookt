package waambokt.commands.reprimand

import dev.kord.core.entity.User
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import waambokt.commands.Reprimand

class ReprimandTest {
    private val expected = "they did a bad thing"

    @RelaxedMockK
    private lateinit var user: User

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `execute reprimand happy path`() = runBlocking {
        Assertions.assertEquals(expected, Reprimand(user, expected))
    }
}
