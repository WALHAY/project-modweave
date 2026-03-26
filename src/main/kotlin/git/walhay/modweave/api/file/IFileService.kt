package git.walhay.modweave.api.file

import git.walhay.modweave.api.version.Version
import org.springframework.web.multipart.MultipartFile

interface IFileService {
  fun uploadFilesTransient(version: Version, files: List<MultipartFile>)
}
