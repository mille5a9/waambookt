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
import waambokt.models.ReprimandLog
import waambokt.repos.MongoRepo.getOne
import waambokt.repos.MongoRepo.pushOne
import waambokt.utils.MongoUtil.mockkCoroutineCollection

class ReprimandTest {
    private val reason = "this is a test"
    private val expected = "test has been reprimanded: $reason"

    @RelaxedMockK
    private lateinit var user: User

    @RelaxedMockK
    private lateinit var event: ChatInputCommandInteractionCreateEvent

    @RelaxedMockK
    private lateinit var db: MongoDatabase

    private val collection: MongoCollection<ReprimandLog> =
        mockkCoroutineCollection()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery {
            user.mention
        } returns "test"

        coEvery {
            user.id
        } returns Snowflake.min
    }

    @Test
    fun `execute new reprimand happy path`() = runBlocking {
        coEvery {
            db.getCollection<ReprimandLog>(any(), any())
        } returns collection

        coEvery {
            collection.coroutine.getOne(any())
        } returns null

        coEvery {
            collection.coroutine.pushOne(any(), any())
        } returns InsertOneResult.unacknowledged()

        Assertions.assertEquals(expected, Reprimand.build(db, event, user, reason).execute())

        coVerify(exactly = 1) {
            collection.coroutine.pushOne(any(), any())
        }
    }
}
