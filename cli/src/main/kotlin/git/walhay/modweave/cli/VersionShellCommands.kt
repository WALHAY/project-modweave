package git.walhay.modweave.cli

import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.version.IVersionService
import git.walhay.modweave.api.version.VersionId
import git.walhay.modweave.api.version.command.VersionCreateCommand
import git.walhay.modweave.api.version.http.dto.VersionResponseDto
import java.util.UUID
import org.springframework.data.domain.PageRequest
import org.springframework.shell.core.command.annotation.Command
import org.springframework.shell.core.command.annotation.Option
import org.springframework.stereotype.Component

@Component
class VersionShellCommands(
    private val versionService: IVersionService,
) : ShellCommandSupport() {
  @Command(name = ["version", "get"], description = "Get version by id.")
  fun versionGet(
      @Option(longName = "id") id: UUID,
  ): Any = renderValue(VersionResponseDto.fromVersion(versionService.getModVersion(VersionId(id))))

  @Command(name = ["version", "list"], description = "List version of a mod.")
  fun versionList(
      @Option(longName = "mod-id") modId: String,
      @Option(longName = "page", defaultValue = "0") page: Int,
      @Option(longName = "size", defaultValue = "20") size: Int,
      @Option(longName = "sort", defaultValue = "id,desc") sort: String,
  ): Any =
      renderPage(
          versionService
              .getModVersions(ModId(modId), PageRequest.of(page, size, parseSort(sort)))
              .map { VersionResponseDto.fromVersion(it) },
      )

  @Command(name = ["version", "create"], description = "Create mod version.")
  fun versionCreate(
      @Option(longName = "mod-id") modId: String,
      @Option(longName = "name") name: String,
      @Option(longName = "changes", required = false) changes: String?,
      @Option(longName = "files") files: String,
  ): Any =
      renderValue(
          VersionResponseDto.fromVersion(
              versionService.createModVersion(
                  ModId(modId),
                  VersionCreateCommand(name, changes, multipartFiles(files)),
              ),
          ),
      )

  @Command(name = ["version", "delete"], description = "Delete version.")
  fun versionDelete(
      @Option(longName = "id") id: UUID,
  ): String {
    versionService.deleteModVersion(VersionId(id))
    return "Version '$id' deleted."
  }
}
