package git.walhay.modweave.cli

import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.command.ModCreateCommand
import git.walhay.modweave.api.user.UserId
import java.nio.file.Path
import org.springframework.shell.core.command.annotation.Command
import org.springframework.shell.core.command.annotation.Option
import org.springframework.stereotype.Component

@Component
class ModShellCommands(
    private val modService: IModService,
) : ShellCommandSupport() {
  @Command(name = ["mods", "list"], description = "List mods with paging.")
  fun modsList(
      @Option(longName = "page", defaultValue = "0") page: Int,
      @Option(longName = "size", defaultValue = "20") size: Int,
      @Option(longName = "name", required = false) name: String?,
      @Option(longName = "sort", defaultValue = "name,asc") sort: String,
  ): Any = renderPage(modService.findModsWithFilter(page, size, name, parseSort(sort)))

  @Command(name = ["mods", "get"], description = "Get mod by id.")
  fun modsGet(
      @Option(longName = "id") id: String,
  ): Any = renderValue(modService.findModById(ModId(id)))

  @Command(name = ["mods", "user"], description = "List mods for user.")
  fun modsUser(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "page", defaultValue = "0") page: Int,
      @Option(longName = "size", defaultValue = "20") size: Int,
      @Option(longName = "sort", defaultValue = "name,asc") sort: String,
  ): Any = renderPage(modService.findModsOfUser(UserId(userId), page, size, parseSort(sort)))

  @Command(name = ["mods", "collection"], description = "List mods in collection.")
  fun modsCollection(
      @Option(longName = "collection-id") collectionId: Long,
      @Option(longName = "page", defaultValue = "0") page: Int,
      @Option(longName = "size", defaultValue = "20") size: Int,
      @Option(longName = "sort", defaultValue = "name,asc") sort: String,
  ): Any =
      renderPage(
          modService.findModsInCollection(CollectionId(collectionId), page, size, parseSort(sort)),
      )

  @Command(name = ["mods", "create"], description = "Create mod.")
  fun modsCreate(
      @Option(longName = "user-id") userId: String,
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
          modService.uploadMod(
              UserId(userId),
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
      )

  @Command(name = ["mods", "delete"], description = "Delete mod.")
  fun modsDelete(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "id") id: String,
  ): String {
    modService.deleteMod(UserId(userId), ModId(id))
    return "Mod '$id' deleted."
  }
}
