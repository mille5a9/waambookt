package waambokt.commands

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.eq
import waambokt.config.Database
import waambokt.data.ReprimandLog

class ReprimandTest {
    private val expected = "test has been reprimanded: they did a bad thing"

    @RelaxedMockK
    private lateinit var user: User

    @RelaxedMockK
    private lateinit var event: ChatInputCommandInteractionCreateEvent

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery {
            user.mention
        } returns "test"

        coEvery {
            user.id
        } returns Snowflake(0)
    }

    @AfterEach
    fun tearDown() {
        runBlocking {
            val reprimandLogs = Database.getDb().getCollection<ReprimandLog>()
            reprimandLogs.deleteOne(ReprimandLog::userId eq 0.toULong())
        }
    }

    @Test
    fun `execute reprimand happy path`() = runBlocking {
        Assertions.assertEquals(expected, Reprimand.build(event, user, expected).execute())
    }
}
