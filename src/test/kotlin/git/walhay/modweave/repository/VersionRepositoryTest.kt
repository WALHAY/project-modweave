package git.walhay.modweave.repository

import git.walhay.modweave.api.game.Game
import git.walhay.modweave.api.game.repository.GameRepository
import git.walhay.modweave.api.game.repository.JpaGameRepository
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.repository.JpaModRepository
import git.walhay.modweave.api.mod.repository.ModRepository
import git.walhay.modweave.api.user.User
import git.walhay.modweave.api.user.repository.JpaUserRepository
import git.walhay.modweave.api.user.repository.UserRepository
import git.walhay.modweave.api.version.Version
import git.walhay.modweave.api.version.repository.JpaVersionRepository
import git.walhay.modweave.api.version.repository.VersionRepository
import git.walhay.modweave.testutils.PostgresTestTemplate
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest

@Import(
    JpaVersionRepository::class,
    JpaUserRepository::class,
    JpaGameRepository::class,
    JpaModRepository::class)
class VersionRepositoryTest : PostgresTestTemplate() {

  @Autowired lateinit var versionRepository: VersionRepository

  @Autowired lateinit var userRepository: UserRepository

  @Autowired lateinit var gameRepository: GameRepository

  @Autowired lateinit var modRepository: ModRepository

  private fun seedMod(): Mod {
    val publisher = userRepository.save(User("publisher", "publisher", "publisher@mail.ru", "pass"))
    val game = gameRepository.save(Game(name = "Game", description = null, imagePath = "img.png"))
    return modRepository.save(
        Mod(
            id = ModId("mod-1"),
            name = "Mod",
            description = null,
            imagePath = "img.png",
            publisherId = publisher.username,
            gameId = game.id))
  }

  private fun seedVersion(): Version {
    val mod = seedMod()
    return versionRepository.save(Version(name = "1.0.0", changes = "init", modId = mod.id))
  }

  @Test
  fun `create version`() {
    val created = seedVersion()
    assertNotNull(created)
    assertTrue(created.id.value > 0)
  }

  @Test
  fun `read version`() {
    val created = seedVersion()

    val page = versionRepository.findVersionsByModId(created.modId, PageRequest.of(0, 10))
    assertNotNull(page)
    assertTrue(page.content.any { it.id.value == created.id.value })
  }

  @Test
  fun `delete version`() {
    val created = seedVersion()

    versionRepository.delete(created.id)

    val page = versionRepository.findVersionsByModId(created.modId, PageRequest.of(0, 10))
    assertTrue(page.content.none { it.id.value == created.id.value })
  }
}
