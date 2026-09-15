package git.walhay.modweave.testutils

import git.walhay.modweave.api.category.Category
import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.collection.Collection
import git.walhay.modweave.api.comment.Comment
import git.walhay.modweave.api.game.Game
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.User
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.Version
import org.springframework.mock.web.MockMultipartFile

object TestFixtures {
  fun category(
      name: String = "gameplay",
      description: String? = "Description",
  ) = Category(CategoryId(name), description)

  fun user(
      username: String = "alice",
      email: String = "alice@example.com",
  ) = User(username, "Alice", email, "encoded-password")

  fun game(id: String = "minecraft") =
      Game(GameId(id), "Minecraft", "Sandbox game", "minecraft/logo.png")

  fun mod(
      id: String = "sodium",
      publisher: UserId = UserId("alice"),
      gameId: GameId = GameId("minecraft"),
      versions: MutableList<Version> = mutableListOf(),
  ) =
      Mod(
          id = ModId(id),
          name = "Sodium",
          description = "Performance mod",
          imagePath = "sodium/logo.png",
          publisherId = publisher,
          gameId = gameId,
          categories = setOf(CategoryId("performance")),
          versions = versions,
      )

  fun version(
      name: String = "1.0.0",
      modId: ModId = ModId("sodium"),
  ) = Version(name, "Initial release", modId)

  fun collection(owner: UserId = UserId("alice")) = Collection("Favorites", "Favorite mods", owner)

  fun comment() =
      Comment(content = "Useful mod", authorId = UserId("alice"), modId = ModId("sodium"))

  fun image(
      filename: String = "logo.png",
      content: String = "image",
  ) = MockMultipartFile("file", filename, "image/png", content.toByteArray())

  fun archive(
      filename: String = "mod.zip",
      content: String = "archive",
  ) = MockMultipartFile("file", filename, "application/zip", content.toByteArray())
}
