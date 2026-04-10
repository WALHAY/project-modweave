package git.walhay.modweave.repository

import git.walhay.modweave.api.game.Game
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.game.repository.GameRepository
import git.walhay.modweave.api.game.repository.JpaGameRepository
import git.walhay.modweave.testutils.PostgresTestTemplate
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(JpaGameRepository::class)
class GameRepositoryTest : PostgresTestTemplate() {

  @Autowired lateinit var gameRepository: GameRepository

  private fun seedGame(): Game =
      gameRepository.save(Game(name = "Skyrim", description = "RPG", imagePath = "images/skyrim.png"))

  @Test
  fun `create game`() {
    val created = seedGame()
    assertNotNull(created)

    val refetched = gameRepository.findById(GameId(created.id.value))
    assertNotNull(refetched)
  }

  @Test
  fun `read game`() {
    val created = seedGame()

    val found = gameRepository.findById(GameId(created.id.value))
    assertNotNull(found)

    assertTrue(gameRepository.existsByIdIgnoreCase(GameId(created.id.value)))
  }

  @Test
  fun `update game`() {
    val created = seedGame()

    val updated =
        gameRepository.save(
            Game(
                id = created.id,
                name = "Skyrim SE",
                description = "RPG",
                imagePath = "images/skyrim.png"))

    val refetched = gameRepository.findById(GameId(updated.id.value))
    assertNotNull(refetched)
    assertTrue(refetched!!.name.contains("SE"))
  }
}
