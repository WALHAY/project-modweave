package git.walhay.modweave.mongo

import org.springframework.context.annotation.Profile
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component

@Component
@Profile("mongodb")
class SequenceService(private val mongoOperations: MongoOperations) {
  fun nextId(sequenceName: String): Long {
    val query = Query(Criteria.where("_id").`is`(sequenceName))
    val update = Update().inc("seq", 1)
    val options = FindAndModifyOptions.options().returnNew(true).upsert(true)
    val seq = mongoOperations.findAndModify(query, update, options, Sequence::class.java)
    return seq?.seq ?: 1L
  }

  data class Sequence(@Id val id: String = "", val seq: Long = 0)
}
