package git.walhay.modweave.api.collection

import git.walhay.modweave.api.collection.dto.CollectionCreateDto
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

  override fun createCollection(username: UserId, dto: CollectionCreateDto): Collection =
      dto.let { (name, description) -> Collection(name, description, username) }
          .also { collectionRepository.save(it) }

  override fun addModToCollection(
      username: UserId,
      collectionId: CollectionId,
      modId: ModId,
      index: Int?
  ): Collection {
    val collection = getCollectionById(collectionId)
    if (username != collection.owner) {
      throw Exception("Wrong user")
    }

    val mod = modService.findModById(modId.value)
    collection.mods.putIfAbsent(index ?: collection.mods.size, mod)
    return collectionRepository.save(collection)
  }

  override fun removeModFromCollection(username: UserId, collectionId: CollectionId, modId: ModId) {
    TODO("Not yet implemented")
  }
}
