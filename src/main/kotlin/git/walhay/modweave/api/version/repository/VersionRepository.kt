package git.walhay.modweave.api.version.repository

import git.walhay.modweave.api.version.Version
import git.walhay.modweave.api.version.VersionId

interface VersionRepository {
  fun save(version: Version): Version

  fun delete(versionId: VersionId)
}
