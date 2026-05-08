package git.walhay.modweave.cli

import git.walhay.modweave.api.file.IFileService
import git.walhay.modweave.api.version.IVersionService
import git.walhay.modweave.api.version.VersionId
import java.util.UUID
import org.springframework.shell.core.command.annotation.Command
import org.springframework.shell.core.command.annotation.Option
import org.springframework.stereotype.Component

@Component
class FileShellCommands(
    private val fileService: IFileService,
    private val versionService: IVersionService,
) : ShellCommandSupport() {
  @Command(name = ["file", "upload-version"], description = "Upload file to existing version.")
  fun fileUploadVersion(
      @Option(longName = "version-id") versionId: UUID,
      @Option(longName = "file") file: String,
  ): String {
    val version = versionService.getModVersion(VersionId(versionId))
    val multipartFiles = multipartFiles(file)
    fileService.uploadVersionFiles(version, multipartFiles)
    return "Uploaded ${multipartFiles.size} file(s) to version '$versionId'."
  }
}
