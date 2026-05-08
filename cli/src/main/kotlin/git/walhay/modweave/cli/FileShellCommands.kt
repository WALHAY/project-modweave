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
  @Command(name = ["files", "upload-version"], description = "Upload files to existing version.")
  fun filesUploadVersion(
      @Option(longName = "version-id") versionId: UUID,
      @Option(longName = "files") files: String,
  ): String {
    val version = versionService.getModVersion(VersionId(versionId))
    val multipartFiles = multipartFiles(files)
    fileService.uploadVersionFiles(version, multipartFiles)
    return "Uploaded ${multipartFiles.size} file(s) to version '$versionId'."
  }
}
