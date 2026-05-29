package git.walhay.modweave.api.collection.repository

import git.walhay.modweave.api.collection.Collection
import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.mongo.SequenceService
import org.springframework.context.annotation.Profile
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Document(collection = "collections")
data class CollectionDocument(
    @Id val id: Long,
    val name: String,
    val description: String? = null,
    val owner: String,
    val modIds: List<String> = emptyList(),
)

interface SpringDataMongoCollectionRepository : MongoRepository<CollectionDocument, Long> {
  fun findByOwner(owner: String): List<CollectionDocument>

  fun findByNameContainingIgnoreCase(name: String): List<CollectionDocument>
}

@Repository
@Profile("mongodb")
class MongoCollectionRepository(
    private val repository: SpringDataMongoCollectionRepository,
    private val seq: SequenceService,
    private val modRepo: git.walhay.modweave.api.mod.repository.ModRepository
) : git.walhay.modweave.api.collection.repository.CollectionRepository {
  private fun CollectionDocument.toDomain(): Collection =
      Collection(
          CollectionId(this.id),
          this.name,
          this.description,
          UserId(this.owner),
          this.modIds
              .mapNotNull { modRepo.findById(git.walhay.modweave.api.mod.ModId(it)) }
              .toMutableList(),
      )

  private fun Collection.toDocument(): CollectionDocument {
    val id = if (this.id.value == 0L) seq.nextId("collection_seq") else this.id.value
    return CollectionDocument(
        id, this.name, this.description, this.owner.value, this.mods.map { it.id.value })
  }

  override fun findById(id: CollectionId): Collection? =
      repository.findById(id.value).map { it.toDomain() }.orElse(null)

  override fun findByOwner(owner: UserId): List<Collection> =
      repository.findByOwner(owner.value).map { it.toDomain() }

  override fun findByNameContainingIgnoreCase(name: String): List<Collection> =
      repository.findByNameContainingIgnoreCase(name).map { it.toDomain() }

  override fun save(collection: Collection): Collection =
      repository.save(collection.toDocument()).toDomain()

  override fun deleteById(id: CollectionId) = repository.deleteById(id.value)
}
