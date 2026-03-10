package git.walhay.modweave.api.version.repository

import git.walhay.modweave.api.version.Version

interface VersionRepository {
    fun save(version: Version): Version
    fun delete(version: Version)
}