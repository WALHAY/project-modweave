package git.walhay.modweave.api.mod

import git.walhay.modweave.api.mod.dto.ModUploadDto
import git.walhay.modweave.api.user.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort

interface IModService {
  fun findModById(modId: String): Mod

  fun findModsWithFilter(page: Int, size: Int, name: String?, sort: Sort): Page<Mod>

  fun uploadMod(username: UserId, dto: ModUploadDto): Mod

  fun deleteMod(username: UserId, modId: String)
}
