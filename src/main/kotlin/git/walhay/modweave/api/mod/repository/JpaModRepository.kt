package git.walhay.modweave.api.mod.repository

import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class JpaModRepository(private val repository: SpringDataModRepository) : ModRepository {
  override fun findById(modId: ModId): Mod? = repository.findByIdOrNull(modId.value)?.toDomain()

  override fun deleteById(modId: ModId) = repository.deleteById(modId.value)

  override fun save(mod: Mod): Mod = repository.save(ModEntity.fromMod(mod)).toDomain()

  override fun findAll(pageable: Pageable): Page<Mod> =
      repository.findAll(pageable).map { it.toDomain() }

  override fun findAll(name: String, pageable: Pageable): Page<Mod> =
      repository.findAllByNameContainingIgnoreCase(name, pageable).map { it.toDomain() }

  override fun findAllByUser(username: UserId, pageable: Pageable): Page<Mod> =
      repository.findAllByPublisherId(username.value, pageable).map { it.toDomain() }

  override fun existsById(modId: ModId): Boolean = repository.existsById(modId.value)
}
