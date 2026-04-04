package git.walhay.modweave.api.version.exception

class VersionExistsException(
    version: String,
) : RuntimeException("Version with name '$version' already exists")