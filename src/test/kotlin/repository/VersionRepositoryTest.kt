package git.walhay.modweave.repository

import git.walhay.modweave.domain.common.paging.PageRequest
import git.walhay.modweave.domain.game.Game
import git.walhay.modweave.domain.game.GameRepository
import git.walhay.modweave.domain.mod.Mod
import git.walhay.modweave.domain.mod.ModId
import git.walhay.modweave.domain.mod.ModRepository
import git.walhay.modweave.domain.user.User
import git.walhay.modweave.domain.user.UserRepository
import git.walhay.modweave.domain.version.Version
import git.walhay.modweave.domain.version.VersionRepository
import git.walhay.modweave.persistence.game.JpaGameRepository
import git.walhay.modweave.persistence.mod.JpaModRepository
import git.walhay.modweave.persistence.user.JpaUserRepository
import git.walhay.modweave.persistence.version.JpaVersionRepository
import git.walhay.modweave.testutils.PostgresTestTemplate
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

@Import(
    JpaVersionRepository::class,
    JpaUserRepository::class,
    JpaGameRepository::class,
    JpaModRepository::class,
)
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
            gameId = game.id,
        ),
    )
  }

  private fun seedVersion(): Version {
    val mod = seedMod()
    return versionRepository.save(Version(name = "1.0.0", changes = "init", modId = mod.id))
  }

  @Test
  fun `create version`() {
    val created = seedVersion()
    assertNotNull(created)
    assertTrue(created.id.value != UUID(0L, 0L))
  }

  @Test
  fun `read version`() {
    val created = seedVersion()

    val page = versionRepository.findVersionsByModId(created.modId, PageRequest(0, 10))
    assertNotNull(page)
    assertTrue(page.content.any { it.id.value == created.id.value })
  }

  @Test
  fun `delete version`() {
    val created = seedVersion()

    versionRepository.delete(created.id)

    val page = versionRepository.findVersionsByModId(created.modId, PageRequest(0, 10))
    assertTrue(page.content.none { it.id.value == created.id.value })
  }
}
