package git.walhay.modweave.cli

import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.game.IGameService
import git.walhay.modweave.api.game.command.GameCreateCommand
import java.nio.file.Path
import org.springframework.shell.core.command.annotation.Command
import org.springframework.shell.core.command.annotation.Option
import org.springframework.stereotype.Component

@Component
class GameShellCommands(
    private val gameService: IGameService,
) : ShellCommandSupport() {
  @Command(name = ["games", "list"], description = "List games with paging.")
  fun gamesList(
      @Option(longName = "page", defaultValue = "0") page: Int,
      @Option(longName = "size", defaultValue = "20") size: Int,
      @Option(longName = "name", required = false) name: String?,
      @Option(longName = "sort", defaultValue = "name,asc") sort: String,
  ): Any = renderPage(gameService.findGamesWithFilter(page, size, name, parseSort(sort)))

  @Command(name = ["games", "get"], description = "Get game by id.")
  fun gamesGet(
      @Option(longName = "id") id: String,
  ): Any = renderValue(gameService.findGameById(GameId(id)))

  @Command(name = ["games", "create"], description = "Create a game.")
  fun gamesCreate(
      @Option(longName = "id") id: String,
      @Option(longName = "name") name: String,
      @Option(longName = "description", required = false) description: String?,
      @Option(longName = "image") imagePath: String,
  ): Any =
      renderValue(
          gameService.uploadGame(
              GameCreateCommand(
                  id = GameId(id),
                  name = name,
                  description = description,
                  image = multipartFile(Path.of(imagePath)),
              ),
          ),
      )

  @Command(name = ["games", "delete"], description = "Delete game by id.")
  fun gamesDelete(
      @Option(longName = "id") id: String,
  ): String {
    gameService.deleteGame(GameId(id))
    return "Game '$id' deleted."
  }
}
