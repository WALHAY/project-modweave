package git.walhay.modweave.api.collection

import git.walhay.modweave.api.collection.command.CollectionCreateCommand
import git.walhay.modweave.api.collection.exception.CollectionNotFoundException
import git.walhay.modweave.api.collection.repository.CollectionRepository
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class CollectionService(
    private val collectionRepository: CollectionRepository,
    private val modService: IModService
) : ICollectionService {
  override fun getCollectionById(id: CollectionId): Collection =
      collectionRepository.findById(id) ?: throw CollectionNotFoundException(id)

  override fun createCollection(userId: UserId, command: CollectionCreateCommand): Collection =
      command
          .let { (name, description) -> Collection(name, description, userId) }
          .let { collectionRepository.save(it) }

  override fun addModToCollection(
      userId: UserId,
      collectionId: CollectionId,
      modId: ModId,
      index: Int?
  ): Collection {
    val collection = getCollectionById(collectionId)
    if (userId != collection.owner) {
      throw Exception("Wrong user")
    }

    val mod = modService.findModById(modId)
    collection.mods.putIfAbsent(index ?: collection.mods.size, mod)
    return collectionRepository.save(collection)
  }

  override fun removeModFromCollection(userId: UserId, collectionId: CollectionId, modId: ModId) {}
}
