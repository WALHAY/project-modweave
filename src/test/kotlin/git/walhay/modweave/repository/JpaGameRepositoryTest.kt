package git.walhay.modweave.api.game.repository

import git.walhay.modweave.api.game.Game
import git.walhay.modweave.testutils.BoundaryConditionTest
import git.walhay.modweave.testutils.StateTransitionTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class JpaGameRepositoryTest {
  private val springData = mock<SpringDataGameRepository>()
  private val repository = JpaGameRepository(springData)

  @StateTransitionTest
  @Test
  fun `saves game and maps entity`() {
    val game = Game(name = "Minecraft", description = "Sandbox", imagePath = "minecraft.png")
    whenever(springData.save(any<GameEntity>())).thenReturn(GameEntity.fromGame(game))

    val saved = repository.save(game)

    assertEquals(game.id.value, saved.id.value)
    assertEquals(game.name, saved.name)
    assertEquals(game.description, saved.description)
    verify(springData).save<GameEntity>(any())
  }

  @BoundaryConditionTest
  @Test
  fun `delegates case insensitive existence check`() {
    whenever(springData.existsByIdIgnoreCase("minecraft")).thenReturn(true)

    assertTrue(repository.existsByIdIgnoreCase(gameId("minecraft")))
    verify(springData).existsByIdIgnoreCase("minecraft")
  }

  private fun gameId(value: String) = git.walhay.modweave.api.game.GameId(value)
}
