package git.walhay.modweave.api.collection

import git.walhay.modweave.api.collection.command.CollectionCreateCommand
import git.walhay.modweave.api.collection.exception.CollectionNotFoundException
import git.walhay.modweave.api.collection.repository.CollectionRepository
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import jakarta.transaction.Transactional
import mu.KLogger
import mu.KotlinLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
@Transactional
class CollectionService(
    private val collectionRepository: CollectionRepository,
    private val modService: IModService,
) : ICollectionService {
  private val logger: KLogger = KotlinLogging.logger {}

  @Cacheable("collections", key = "#id.value")
  override fun getCollectionById(id: CollectionId): Collection {
    logger.debug { "Fetching collection by id: $id" }
    return collectionRepository.findById(id) ?: throw CollectionNotFoundException(id)
  }

  @CachePut("collections", key = "#result.id.value")
  override fun createCollection(
      userId: UserId,
      command: CollectionCreateCommand,
  ): Collection {
    logger.info { "Creating new collection: ${command.name} for user: $userId" }
    return command
        .let { (name, description) -> Collection(name, description, userId) }
        .let { collectionRepository.save(it) }
        .also { logger.info { "Collection created successfully: ${it.id}" } }
  }

  @CacheEvict("collections", key = "#collectionId.value")
  override fun deleteCollection(
      userId: UserId,
      collectionId: CollectionId,
  ) {
    logger.info { "Deleting collection: $collectionId for user: $userId" }
    val collection = getCollectionById(collectionId)
    if (collection.owner == userId) {
      logger.warn { "Collection deletion failed - forbidden for user: $userId" }
      throw Exception("Forbidden")
    }

    collectionRepository.deleteById(collectionId)
    logger.info { "Collection deleted successfully: $collectionId" }
  }

  override fun addModToCollection(
      userId: UserId,
      collectionId: CollectionId,
      modId: ModId,
      index: Int?,
  ): Collection {
    logger.info {
      "Adding mod: $modId to collection: $collectionId for user: $userId at index: $index"
    }
    val collection = getCollectionById(collectionId)
    if (userId != collection.owner) {
      logger.warn { "Add mod failed - wrong user for collection: $collectionId" }
      throw Exception("Wrong user")
    }

    val mod = modService.findModById(modId)
    collection.mods.addLast(mod)
    val result = collectionRepository.save(collection)
    logger.info { "Mod added successfully to collection: $collectionId" }
    return result
  }

  override fun deleteModFromCollection(
      userId: UserId,
      collectionId: CollectionId,
      modId: ModId,
  ) {
    logger.info { "Deleting mod: $modId from collection: $collectionId for user: $userId" }
  }
}
