package waambokt.utils

import com.mongodb.reactivestreams.client.MongoCollection
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.litote.kmongo.coroutine.coroutine
import kotlin.reflect.KClass

object MongoUtil {
    inline fun <reified T : Any> mockkCoroutineCollection(
        name: String? = null,
        relaxed: Boolean = false,
        vararg moreInterfaces: KClass<*>,
        relaxUnitFun: Boolean = false,
        block: MongoCollection<T>.() -> Unit = {}
    ): MongoCollection<T> = mockk(name, relaxed, *moreInterfaces, relaxUnitFun = relaxUnitFun) {
        mockkStatic(MongoCollection<*>::coroutine)
        val that = this
        every { coroutine } returns mockk(name, relaxed, *moreInterfaces, relaxUnitFun = relaxUnitFun) {
            every { collection } returns that
        }
        block()
    }
}
