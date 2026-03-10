package git.walhay.modweave.api.mod.repository

import git.walhay.modweave.api.mod.Mod
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ModRepository {
    fun findById(id: String): Mod?
    fun findAll(pageable: Pageable): Page<Mod>
	fun findAll(name: String, pageable: Pageable): Page<Mod>
    fun existsById(id: String): Boolean
    fun save(mod: Mod): Mod
    fun deleteById(modId: String)
}