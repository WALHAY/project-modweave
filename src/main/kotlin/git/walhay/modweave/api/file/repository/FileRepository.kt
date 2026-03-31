package git.walhay.modweave.api.file.repository

import git.walhay.modweave.api.file.File

interface FileRepository {
  fun save(file: File): File
}
