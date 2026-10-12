package git.walhay.modweave.domain.version

class VersionExistsException(
    version: String,
) : RuntimeException("Version with name '$version' already exists")
