package git.walhay.modweave.repository

import git.walhay.modweave.domain.common.paging.PageRequest
import git.walhay.modweave.domain.common.paging.SortDirection
import git.walhay.modweave.domain.common.paging.SortOrder
import git.walhay.modweave.domain.game.Game
import git.walhay.modweave.domain.game.GameRepository
import git.walhay.modweave.domain.mod.Mod
import git.walhay.modweave.domain.mod.ModId
import git.walhay.modweave.domain.mod.ModRepository
import git.walhay.modweave.domain.user.User
import git.walhay.modweave.domain.user.UserId
import git.walhay.modweave.domain.user.UserRepository
import git.walhay.modweave.persistence.game.JpaGameRepository
import git.walhay.modweave.persistence.mod.JpaModRepository
import git.walhay.modweave.persistence.user.JpaUserRepository
import git.walhay.modweave.testutils.PostgresTestTemplate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

@Import(JpaModRepository::class, JpaUserRepository::class, JpaGameRepository::class)
class ModRepositoryTest : PostgresTestTemplate() {
  @Autowired lateinit var modRepository: ModRepository

  @Autowired lateinit var userRepository: UserRepository

  @Autowired lateinit var gameRepository: GameRepository

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
            gameId = game.id,
        )

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

    modRepository.findAll(PageRequest(0, 10))
    modRepository.findAll("mod", PageRequest(0, 10))
    modRepository.findAllByUser(UserId("publisher"), PageRequest(0, 10))
  }

  @Test
  fun `domain pagination preserves offset mixed sorting and page metadata in PostgreSQL`() {
    val original = seedMod()
    modRepository.save(original.copy(name = "beta"))
    modRepository.save(original.copy(id = ModId("mod-2"), name = "Alpha"))
    modRepository.save(original.copy(id = ModId("mod-3"), name = "Beta"))
    modRepository.save(original.copy(id = ModId("mod-4"), name = "alpha"))
    modRepository.save(original.copy(id = ModId("mod-5"), name = "BETA"))

    val page =
        modRepository.findAll(
            PageRequest(
                page = 1,
                size = 2,
                sort =
                    listOf(
                        SortOrder("name", SortDirection.DESC, ignoreCase = true), SortOrder("id"))))

    assertEquals(listOf(ModId("mod-5"), ModId("mod-2")), page.content.map { it.id })
    assertEquals(1, page.page)
    assertEquals(2, page.size)
    assertEquals(5L, page.totalElements)
    assertEquals(3, page.totalPages)
    assertFalse(page.isEmpty)
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
