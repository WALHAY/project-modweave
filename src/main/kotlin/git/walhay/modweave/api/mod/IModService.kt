package git.walhay.modweave.api.mod

import git.walhay.modweave.api.mod.dto.ModUploadDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort

interface IModService {
  fun findModById(id: String): Mod

  fun findModsWithFilter(page: Int, size: Int, name: String?, sort: Sort): Page<Mod>

  fun uploadMod(login: String, dto: ModUploadDto): Mod

  fun deleteMod(login: String, modId: String)
}
