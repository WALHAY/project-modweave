package git.walhay.modweave.api.mod.repository

import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.toEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class JpaModRepository(private val repository: SpringDataModRepository) : ModRepository {
    override fun findById(id: String): Mod? = repository.findByIdOrNull(id)?.toModel()
    override fun deleteById(modId: String) = repository.deleteById(modId)

    override fun save(mod: Mod): Mod = repository.save(mod.toEntity()).toModel()

    override fun findAll(pageable: Pageable): Page<Mod> = repository.findAll(pageable).map { it.toModel() }

    override fun findAll(
        name: String,
        pageable: Pageable
    ): Page<Mod> = repository.findAllByNameContainingIgnoreCase(name, pageable).map { it.toModel() }

    override fun existsById(id: String): Boolean = repository.existsById(id)


}