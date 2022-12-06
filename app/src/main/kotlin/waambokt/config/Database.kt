package waambokt.config

import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

object Database {
    fun getDb() =
        KMongo.createClient().coroutine.getDatabase(
            if (Env.isProd) "prod" else "test"
        )
}
