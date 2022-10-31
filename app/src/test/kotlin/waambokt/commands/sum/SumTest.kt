package waambokt.commands.sum

import kotlinx.coroutines.runBlocking
import org.junit.Test
import waambokt.commands.sum.Sum.execute
import kotlin.test.assertEquals

class SumTest {

    private val first: Long = 4
    private val second: Long = 5
    private val sum: Long = 9
    private val expected = "$first + $second = $sum"

    @Test
    fun `execute Sum happy path`() = runBlocking {
        val response = listOf(first, second).execute()
        assertEquals(expected, response)
    }
}
