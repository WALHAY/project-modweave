package git.walhay.modweave.api.mod.repository

import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ModRepository {
  fun findById(modId: ModId): Mod?

  fun findAll(pageable: Pageable): Page<Mod>

  fun findAll(name: String, pageable: Pageable): Page<Mod>

  fun existsById(modId: ModId): Boolean

  fun save(mod: Mod): Mod

  fun deleteById(modId: ModId)
}
