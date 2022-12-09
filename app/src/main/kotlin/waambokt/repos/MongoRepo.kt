package waambokt.repos

import com.mongodb.client.model.InsertOneOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import org.bson.conversions.Bson
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.util.KMongoUtil.idFilterQuery
import org.litote.kmongo.util.KMongoUtil.toBsonModifier
import org.litote.kmongo.util.UpdateConfiguration

object MongoRepo {
    suspend fun <T : Any> CoroutineCollection<T>.getOne(
        vararg filter: Bson
    ): T? {
        return this.findOne(*filter)
    }

    suspend fun <T : Any> CoroutineCollection<T>.pushOne(
        document: T,
        options: InsertOneOptions = InsertOneOptions()
    ): InsertOneResult {
        return this.insertOne(document, options)
    }

    suspend fun <T : Any> CoroutineCollection<T>.overwriteOneById(
        id: Any,
        document: T,
        options: UpdateOptions = UpdateOptions()
    ): UpdateResult {
        return this.updateOne(
            idFilterQuery(id),
            toBsonModifier(document, UpdateConfiguration.updateOnlyNotNullProperties),
            options
        )
    }
}
