package git.walhay.modweave.cli

import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.command.ModCreateCommand
import git.walhay.modweave.api.mod.http.dto.ModResponseDto
import git.walhay.modweave.api.mod.http.dto.fromMod
import java.nio.file.Path
import org.springframework.shell.core.command.annotation.Command
import org.springframework.shell.core.command.annotation.Option
import org.springframework.stereotype.Component

@Component
class ModShellCommands(
    private val modService: IModService,
    private val authSession: CliAuthSession,
) : ShellCommandSupport() {
  @Command(name = ["mod", "list"], description = "List mod with paging.")
  fun modList(
      @Option(longName = "page", defaultValue = "0") page: Int,
      @Option(longName = "size", defaultValue = "20") size: Int,
      @Option(longName = "name", required = false) name: String?,
      @Option(longName = "sort", defaultValue = "name,asc") sort: String,
  ): Any =
      renderPage(modService.findModsWithFilter(page, size, name, parseSort(sort)).map {
        ModResponseDto.fromMod(it)
      })

  @Command(name = ["mod", "get"], description = "Get mod by id.")
  fun modGet(
      @Option(longName = "id") id: String,
  ): Any = renderValue(ModResponseDto.fromMod(modService.findModById(ModId(id))))

  @Command(name = ["mod", "user"], description = "List mod for user.")
  fun modUser(
      @Option(longName = "user-id", required = false) userId: String?,
      @Option(longName = "page", defaultValue = "0") page: Int,
      @Option(longName = "size", defaultValue = "20") size: Int,
      @Option(longName = "sort", defaultValue = "name,asc") sort: String,
  ): Any =
      renderPage(modService.findModsOfUser(authSession.resolveUserId(userId), page, size, parseSort(sort)).map {
        ModResponseDto.fromMod(it)
      })

  @Command(name = ["mod", "collection"], description = "List mod in collection.")
  fun modCollection(
      @Option(longName = "collection-id") collectionId: Long,
      @Option(longName = "page", defaultValue = "0") page: Int,
      @Option(longName = "size", defaultValue = "20") size: Int,
      @Option(longName = "sort", defaultValue = "name,asc") sort: String,
  ): Any =
      renderPage(
          modService
              .findModsInCollection(CollectionId(collectionId), page, size, parseSort(sort))
              .map { ModResponseDto.fromMod(it) },
      )

  @Command(name = ["mod", "create"], description = "Create mod.")
  fun modCreate(
      @Option(longName = "user-id", required = false) userId: String?,
      @Option(longName = "id") id: String,
      @Option(longName = "name") name: String,
      @Option(longName = "description", required = false) description: String?,
      @Option(longName = "image") imagePath: String,
      @Option(longName = "version-name") versionName: String,
      @Option(longName = "game-id") gameId: String,
      @Option(longName = "categories", required = false) categories: String?,
      @Option(longName = "files") files: String,
  ): Any =
      renderValue(
          ModResponseDto.fromMod(
              modService.uploadMod(
                  authSession.resolveUserId(userId),
                  ModCreateCommand(
                      id = ModId(id),
                      name = name,
                      description = description,
                      image = multipartFile(Path.of(imagePath)),
                      categories = categoryIds(categories),
                      versionName = versionName,
                      files = multipartFiles(files),
                      gameId = GameId(gameId),
                  ),
              ),
          ),
      )

  @Command(name = ["mod", "delete"], description = "Delete mod.")
  fun modDelete(
      @Option(longName = "user-id", required = false) userId: String?,
      @Option(longName = "id") id: String,
  ): String {
    modService.deleteMod(authSession.resolveUserId(userId), ModId(id))
    return "Mod '$id' deleted."
  }
}
