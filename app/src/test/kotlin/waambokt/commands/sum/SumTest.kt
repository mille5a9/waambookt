package waambokt.commands.sum

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import waambokt.commands.Sum

class SumTest {

    private val first: Long = 4
    private val second: Long = 5
    private val sum: Long = 9
    private val expected = "$first + $second = $sum"

    @Test
    fun `execute Sum happy path`() = runBlocking {
        Assertions.assertEquals(expected, Sum(first, second))
    }
}
