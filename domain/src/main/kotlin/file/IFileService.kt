package git.walhay.modweave.domain.file

import git.walhay.modweave.domain.version.Version
import org.springframework.web.multipart.MultipartFile

interface IFileService {
  fun uploadVersionFiles(
      version: Version,
      files: List<MultipartFile>,
  )
}
