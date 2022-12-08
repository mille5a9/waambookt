package waambokt.commands

import com.mongodb.client.result.InsertOneResult
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.coroutine.coroutine
import waambokt.data.ReprimandLog
import waambokt.util.MongoUtil

class ReprimandTest {
    private val reason = "they did a bad thing"
    private val expected = "test has been reprimanded: they did a bad thing"

    @RelaxedMockK
    private lateinit var user: User

    @RelaxedMockK
    private lateinit var event: ChatInputCommandInteractionCreateEvent

    @RelaxedMockK
    private lateinit var db: MongoDatabase

    private val records: MongoCollection<ReprimandLog> =
        MongoUtil.mockkCoroutineCollection()

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

    @Test
    fun `execute new reprimand happy path`() = runBlocking {
        coEvery {
            db.getCollection<ReprimandLog>(any(), any())
        } returns records

        coEvery {
            records.coroutine.findOne(any<String>())
        } returns null

        coEvery {
            records.coroutine.insertOne(any<ReprimandLog>(), any())
        } returns InsertOneResult.unacknowledged()

        Assertions.assertEquals(expected, Reprimand.build(db, event, user, reason).execute())

        coVerify(exactly = 1) {
            records.coroutine.insertOne(any<ReprimandLog>(), any())
        }
    }
}
