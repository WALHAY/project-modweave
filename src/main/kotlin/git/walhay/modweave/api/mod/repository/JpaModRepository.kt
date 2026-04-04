package git.walhay.modweave.api.mod.repository

import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.toEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class JpaModRepository(private val repository: SpringDataModRepository) : ModRepository {
  override fun findById(modId: ModId): Mod? = repository.findByIdOrNull(modId.value)?.toModel()

  override fun deleteById(modId: ModId) = repository.deleteById(modId.value)

  override fun save(mod: Mod): Mod = repository.save(mod.toEntity()).toModel()

  override fun findAll(pageable: Pageable): Page<Mod> =
      repository.findAll(pageable).map { it.toModel() }

  override fun findAll(name: String, pageable: Pageable): Page<Mod> =
      repository.findAllByNameContainingIgnoreCase(name, pageable).map { it.toModel() }

  override fun existsById(modId: ModId): Boolean = repository.existsById(modId.value)
}
