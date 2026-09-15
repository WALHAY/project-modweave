package git.walhay.modweave.api.mod.repository

import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.testutils.BoundaryConditionTest
import git.walhay.modweave.testutils.StateTransitionTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class JpaModRepositoryTest {
  private val springData = mock<SpringDataModRepository>()
  private val repository = JpaModRepository(springData)

  @StateTransitionTest
  @Test
  fun `saves mod and maps entity`() {
    val mod = mod()
    whenever(springData.save(any<ModEntity>())).thenReturn(ModEntity.fromMod(mod))

    assertEquals(mod, repository.save(mod))
    verify(springData).save<ModEntity>(any())
  }

  @BoundaryConditionTest
  @Test
  fun `checks mod existence`() {
    whenever(springData.existsById("sodium")).thenReturn(true)

    assertTrue(repository.existsById(ModId("sodium")))
    verify(springData).existsById("sodium")
  }

  private fun mod() =
      Mod(
          id = ModId("sodium"),
          name = "Sodium",
          imagePath = "sodium.png",
          publisherId = UserId("alice"),
          gameId = GameId("minecraft"),
      )
}
