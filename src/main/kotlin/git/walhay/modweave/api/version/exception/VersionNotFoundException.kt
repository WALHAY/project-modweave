package git.walhay.modweave.api.version.exception

import git.walhay.modweave.api.version.VersionId

class VersionNotFoundException(versionId: VersionId) :
    Exception("Version with id=\"$versionId\" not found")
