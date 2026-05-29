package git.walhay.modweave.api.mod.repository

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.Version
import git.walhay.modweave.api.version.VersionId
import git.walhay.modweave.api.version.VersionStatus
import org.springframework.context.annotation.Profile
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

// Documents used only for Mongo persistence mapping
@Document(collection = "mods")
data class ModDocument(
    @Id val id: String,
    val name: String,
    val description: String? = null,
    val imagePath: String,
    val creationDate: java.time.LocalDateTime,
    val publisherId: String,
    val gameId: String,
    val categories: Set<String> = emptySet(),
    val versions: List<VersionDocument> = emptyList(),
)

@Document(collection = "versions")
data class VersionDocument(
    @Id val id: java.util.UUID,
    val name: String,
    val changes: String? = null,
    val uploadDate: java.time.LocalDateTime,
    val status: String,
    val modId: String,
    val fileIds: List<Long> = emptyList(),
)

@Document(collection = "collection_items")
data class CollectionItemDocument(
    @Id val id: String? = null,
    val orderIndex: Int = 0,
    val collectionId: Long = 0,
    val modId: String = "",
)

interface SpringDataMongoModRepository : MongoRepository<ModDocument, String> {
  fun findAllByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<ModDocument>

  fun findAllByPublisherId(id: String, pageable: Pageable): Page<ModDocument>
}

interface SpringDataMongoCollectionItemRepository :
    MongoRepository<CollectionItemDocument, String> {
  fun findByCollectionId(collectionId: Long, pageable: Pageable): Page<CollectionItemDocument>
}

@Repository
@Profile("mongodb")
class MongoModRepository(
    private val repository: SpringDataMongoModRepository,
    private val collectionItemRepository: SpringDataMongoCollectionItemRepository,
) : ModRepository {
  private fun VersionDocument.toDomain(): Version =
      Version(
          id = VersionId(this.id),
          name = this.name,
          changes = this.changes,
          uploadDate = this.uploadDate,
          status = VersionStatus.valueOf(this.status),
          modId = ModId(this.modId),
      )

  private fun Version.toDocument(): VersionDocument =
      VersionDocument(
          id = this.id.value,
          name = this.name,
          changes = this.changes,
          uploadDate = this.uploadDate,
          status = this.status.name,
          modId = this.modId.value,
          fileIds = this.files.map { it.id.value },
      )

  private fun ModDocument.toDomain(): Mod =
      Mod(
          ModId(this.id),
          this.name,
          this.description,
          this.imagePath,
          this.creationDate,
          UserId(this.publisherId),
          GameId(this.gameId),
          this.categories.map { CategoryId(it) }.toSet(),
          this.versions.map { it.toDomain() }.toMutableList(),
      )

  private fun Mod.toDocument(): ModDocument =
      ModDocument(
          id = this.id.value,
          name = this.name,
          description = this.description,
          imagePath = this.imagePath,
          creationDate = this.creationDate,
          publisherId = this.publisherId.value,
          gameId = this.gameId.value,
          categories = this.categories.map { it.value }.toSet(),
          versions = this.versions.map { it.toDocument() },
      )

  override fun findById(modId: ModId): Mod? =
      repository.findById(modId.value).map { it.toDomain() }.orElse(null)

  override fun deleteById(modId: ModId) = repository.deleteById(modId.value)

  override fun save(mod: Mod): Mod = repository.save(mod.toDocument()).toDomain()

  override fun findAll(pageable: Pageable): Page<Mod> =
      repository.findAll(pageable).map { it.toDomain() }

  override fun findAll(name: String, pageable: Pageable): Page<Mod> =
      repository.findAllByNameContainingIgnoreCase(name, pageable).map { it.toDomain() }

  override fun findAllByUser(username: UserId, pageable: Pageable): Page<Mod> =
      repository.findAllByPublisherId(username.value, pageable).map { it.toDomain() }

  override fun findModsInCollection(collectionId: CollectionId, pageable: Pageable): Page<Mod> {
    val items =
        collectionItemRepository.findByCollectionId(
            collectionId.value, PageRequest.of(0, Integer.MAX_VALUE))
    val modIds = items.content.map { it.modId }
    if (modIds.isEmpty()) return PageImpl(emptyList(), pageable, 0)
    val mods = repository.findAllById(modIds).map { it.toDomain() }
    val start = pageable.offset.toInt()
    val end = (start + pageable.pageSize).coerceAtMost(mods.size)
    val pageContent = if (start <= end) mods.subList(start, end) else emptyList()
    return PageImpl(pageContent, pageable, mods.size.toLong())
  }

  override fun existsById(modId: ModId): Boolean = repository.existsById(modId.value)
}
