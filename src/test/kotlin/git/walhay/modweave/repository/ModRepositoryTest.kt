package git.walhay.modweave.repository

import git.walhay.modweave.api.game.Game
import git.walhay.modweave.api.game.repository.JpaGameRepository
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.repository.JpaModRepository
import git.walhay.modweave.api.mod.repository.ModRepository
import git.walhay.modweave.api.user.User
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.user.repository.JpaUserRepository
import git.walhay.modweave.api.user.repository.UserRepository
import git.walhay.modweave.testutils.PostgresTestTemplate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest

@Import(JpaModRepository::class, JpaUserRepository::class, JpaGameRepository::class)
class ModRepositoryTest : PostgresTestTemplate() {

  @Autowired lateinit var modRepository: ModRepository

  @Autowired lateinit var userRepository: UserRepository

  @Autowired lateinit var gameRepository: git.walhay.modweave.api.game.repository.GameRepository

  private fun seedPublisher(): User =
      userRepository.save(User("publisher", "publisher", "publisher@mail.ru", "pass"))

  private fun seedGame(): Game =
      gameRepository.save(Game(name = "Game", description = null, imagePath = "img.png"))

  private fun seedMod(): Mod {
    val publisher = seedPublisher()
    val game = seedGame()

    val mod =
        Mod(
            id = ModId("mod-1"),
            name = "My Mod",
            description = "desc",
            imagePath = "img.png",
            publisherId = publisher.username,
            gameId = game.id)

    return modRepository.save(mod)
  }

  @Test
  fun `create mod`() {
    val created = seedMod()
    assertNotNull(created)
    assertEquals("mod-1", created.id.value)
  }

  @Test
  fun `read mod`() {
    val created = seedMod()

    val found = modRepository.findById(created.id)
    assertNotNull(found)
    assertTrue(modRepository.existsById(created.id))

    // List queries should work without blowing up
    modRepository.findAll(PageRequest.of(0, 10))
    modRepository.findAll("mod", PageRequest.of(0, 10))
    modRepository.findAllByUser(UserId("publisher"), PageRequest.of(0, 10))
  }

  @Test
  fun `update mod`() {
    val created = seedMod()

    val updated = modRepository.save(created.copy(name = "My Mod 2"))
    assertTrue(updated.name.contains("2"))

    val refetched = modRepository.findById(created.id)
    assertNotNull(refetched)
    assertEquals("My Mod 2", refetched!!.name)
  }

  @Test
  fun `delete mod`() {
    val created = seedMod()

    modRepository.deleteById(created.id)

    val after = modRepository.findById(created.id)
    assertNull(after)
  }
}
