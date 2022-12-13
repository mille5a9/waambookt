package waambokt.config

import com.mongodb.reactivestreams.client.MongoDatabase
import org.litote.kmongo.reactivestreams.KMongo

object Database {
    fun getDb(): MongoDatabase =
        KMongo.createClient(Env.mongo).getDatabase(if (Env.isProd) "prodkt" else "testkt")
}
