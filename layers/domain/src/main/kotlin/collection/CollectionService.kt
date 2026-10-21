package git.walhay.modweave.domain.collection

import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.common.paging.PageSizePolicy
import git.walhay.modweave.domain.mod.ModId
import git.walhay.modweave.domain.mod.ModService
import git.walhay.modweave.domain.user.UserId
import mu.KLogger
import mu.KotlinLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CollectionService(
    private val collectionRepository: CollectionRepository,
    private val pageSizePolicy: PageSizePolicy,
    private val modService: ModService,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @Cacheable("collections", key = "#p0")
  fun getCollectionById(id: CollectionId): Collection {
    logger.debug { "Fetching collection by id: $id" }
    return collectionRepository.findById(id) ?: throw CollectionNotFoundException(id)
  }

  @CacheEvict("collections", allEntries = true)
  @PreAuthorize("@accessSecurity.isSelf(#p0)")
  fun createCollection(
      userId: UserId,
      command: CollectionCreateCommand,
  ): Collection {
    logger.info { "Creating new collection: ${command.name} for user: $userId" }
    return command
        .let { (name, description) -> Collection(name, description, userId) }
        .let { collectionRepository.save(it) }
        .also { logger.info { "Collection created successfully: ${it.id}" } }
  }

  @CacheEvict("collections", key = "#p1")
  @PreAuthorize("@accessSecurity.isCollectionOwnerOrAdmin(#p0, #p1)")
  fun deleteCollection(
      userId: UserId,
      collectionId: CollectionId,
  ) {
    logger.info { "Deleting collection: $collectionId for user: $userId" }
    collectionRepository.deleteById(collectionId)
    logger.info { "Collection deleted successfully: $collectionId" }
  }

  @CacheEvict("collections", key = "#p1")
  @PreAuthorize("@accessSecurity.isCollectionOwnerOrAdmin(#p0, #p1)")
  fun addModToCollection(
      userId: UserId,
      collectionId: CollectionId,
      modId: ModId,
      index: Int?,
  ): Collection {
    logger.info {
      "Adding mod: $modId to collection: $collectionId for user: $userId at index: $index"
    }
    val collection = getCollectionById(collectionId)

    require(collection.mods.none { it.id == modId }) { "Mod already belongs to the collection" }
    val insertionIndex = index ?: collection.mods.size
    require(insertionIndex in 0..collection.mods.size) { "Collection index is out of bounds" }
    val mod = modService.findModById(modId)
    collection.mods.add(insertionIndex, mod)
    val result = collectionRepository.save(collection)
    logger.info { "Mod added successfully to collection: $collectionId" }
    return result
  }

  @CacheEvict("collections", key = "#p1")
  @PreAuthorize("@accessSecurity.isCollectionOwnerOrAdmin(#p0, #p1)")
  fun deleteModFromCollection(
      userId: UserId,
      collectionId: CollectionId,
      modId: ModId,
  ) {
    logger.info { "Deleting mod: $modId from collection: $collectionId for user: $userId" }
    val collection = getCollectionById(collectionId)
    collection.mods.removeAll { it.id == modId }
    collectionRepository.save(collection)
  }

  fun findCollectionsOfUser(
      id: UserId,
      page: Int,
      size: Int,
      sort: Sort,
  ): Page<Collection> =
      collectionRepository.findAllByUser(
          id, PageRequest.of(page, pageSizePolicy.normalize(size), sort))
}
