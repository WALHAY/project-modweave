package git.walhay.modweave.repository

import git.walhay.modweave.api.collection.Collection
import git.walhay.modweave.api.collection.repository.CollectionRepository
import git.walhay.modweave.api.collection.repository.JpaCollectionRepository
import git.walhay.modweave.api.game.Game
import git.walhay.modweave.api.game.repository.JpaGameRepository
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.repository.JpaModRepository
import git.walhay.modweave.api.user.User
import git.walhay.modweave.api.user.repository.JpaUserRepository
import git.walhay.modweave.api.user.repository.UserRepository
import git.walhay.modweave.testutils.PostgresTestTemplate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

@Import(
    JpaCollectionRepository::class,
    JpaUserRepository::class,
    JpaGameRepository::class,
    JpaModRepository::class)
class CollectionRepositoryTest : PostgresTestTemplate() {

  @Autowired lateinit var collectionRepository: CollectionRepository

  @Autowired lateinit var userRepository: UserRepository

  @Autowired lateinit var gameRepository: git.walhay.modweave.api.game.repository.GameRepository

  @Autowired lateinit var modRepository: git.walhay.modweave.api.mod.repository.ModRepository

  private fun seedUser(): User =
      userRepository.save(User("owner", "owner", "owner@mail.ru", "pass"))

  private fun seedGame(): Game =
      gameRepository.save(Game(name = "Game", description = null, imagePath = "img.png"))

  private fun seedMod(owner: User, game: Game): Mod =
      modRepository.save(
          Mod(
              id = ModId("mod1"),
              name = "Mod 1",
              description = null,
              imagePath = "img.png",
              publisherId = owner.username,
              gameId = game.id))

  private fun seedCollection(): Collection {
    val owner = seedUser()
    val game = seedGame()
    val mod = seedMod(owner, game)

    return collectionRepository.save(
        Collection(
            name = "Favorites",
            description = "fav mods",
            owner = owner.username,
            mods = mutableListOf(mod)))
  }

  @Test
  fun `create collection`() {
    val created = seedCollection()
    assertNotNull(created)
    assertTrue(created.id.value > 0)
  }

  @Test
  fun `read collection`() {
    val created = seedCollection()

    val found = collectionRepository.findById(created.id)
    assertNotNull(found)
    assertEquals(created.name, found!!.name)
  }

  @Test
  fun `update collection`() {
    val created = seedCollection()

    val updated = collectionRepository.save(created.copy(name = "Favorites 2"))
    assertNotNull(updated)
    assertEquals(created.id.value, updated.id.value)

    val refetched = collectionRepository.findById(updated.id)
    assertNotNull(refetched)
    assertEquals("Favorites 2", refetched!!.name)
  }

  @Test
  fun `delete collection`() {
    val created = seedCollection()

    collectionRepository.deleteById(created.id)

    val after = collectionRepository.findById(created.id)
    assertNull(after)
  }
}
