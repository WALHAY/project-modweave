package git.walhay.modweave.api.collection

import git.walhay.modweave.api.collection.command.CollectionCreateCommand
import git.walhay.modweave.api.collection.exception.CollectionNotFoundException
import git.walhay.modweave.api.collection.repository.CollectionRepository
import git.walhay.modweave.api.common.paging.PageSizePolicy
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import jakarta.transaction.Transactional
import mu.KLogger
import mu.KotlinLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
@Transactional
class CollectionService(
    private val collectionRepository: CollectionRepository,
    private val pageSizePolicy: PageSizePolicy,
    private val modService: IModService,
) : ICollectionService {
  private val logger: KLogger = KotlinLogging.logger {}

  @Cacheable("collections", key = "#id")
  override fun getCollectionById(id: CollectionId): Collection {
    logger.debug { "Fetching collection by id: $id" }
    return collectionRepository.findById(id) ?: throw CollectionNotFoundException(id)
  }

  @CachePut("collections", key = "#result.id")
  @PreAuthorize("isAuthenticated()")
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

  @CacheEvict("collections", key = "#collectionId")
  @PreAuthorize("@accessSecurity.isCollectionOwnerOrAdmin(#userId, #collectionId)")
  override fun deleteCollection(
      userId: UserId,
      collectionId: CollectionId,
  ) {
    logger.info { "Deleting collection: $collectionId for user: $userId" }
    collectionRepository.deleteById(collectionId)
    logger.info { "Collection deleted successfully: $collectionId" }
  }

  @PreAuthorize("@accessSecurity.isCollectionOwnerOrAdmin(#userId, #collectionId)")
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

    val mod = modService.findModById(modId)
    collection.mods.addLast(mod)
    val result = collectionRepository.save(collection)
    logger.info { "Mod added successfully to collection: $collectionId" }
    return result
  }

  @PreAuthorize("@accessSecurity.isCollectionOwnerOrAdmin(#userId, #collectionId)")
  override fun deleteModFromCollection(
      userId: UserId,
      collectionId: CollectionId,
      modId: ModId,
  ) {
    logger.info { "Deleting mod: $modId from collection: $collectionId for user: $userId" }
  }

  override fun findCollectionsOfUser(
      id: UserId,
      page: Int,
      size: Int,
      sort: Sort,
  ): Page<Collection> =
      collectionRepository.findAllByUser(
          id, PageRequest.of(page, pageSizePolicy.normalize(size), sort))
}
