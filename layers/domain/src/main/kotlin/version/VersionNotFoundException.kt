package git.walhay.modweave.domain.version

class VersionNotFoundException(
    versionId: VersionId,
) : RuntimeException("Version with id=\"$versionId\" not found")
