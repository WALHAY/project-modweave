package git.walhay.modweave.service

import git.walhay.modweave.api.game.*
import git.walhay.modweave.api.game.command.GameCreateCommand
import git.walhay.modweave.api.game.exception.*
import git.walhay.modweave.api.game.repository.GameRepository
import git.walhay.modweave.api.storage.ISimpleStorageService
import git.walhay.modweave.testutils.StateTransitionTest
import git.walhay.modweave.testutils.TestFixtures
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

@StateTransitionTest
class GameServiceTest : ServiceTestSupport() {
  private val repository = mock<GameRepository>()
  private val storage = mock<ISimpleStorageService>()
  private val service = GameService(repository, storage, pagePolicy)

  @Test
  fun `finds game`() {
    val game = TestFixtures.game()
    whenever(repository.findById(game.id)).thenReturn(game)

    assertSame(game, service.findGameById(game.id))
  }

  @Test
  fun `throws for missing game`() {
    val id = GameId("missing")
    whenever(repository.findById(id)).thenReturn(null)

    assertThrows(GameNotFoundException::class.java) { service.findGameById(id) }
  }

  @Test
  fun `filters games without name`() {
    val page = PageImpl(listOf(TestFixtures.game()))
    whenever(repository.findAll(any<PageRequest>())).thenReturn(page)

    assertSame(page, service.findGamesWithFilter(0, 10, null, sort))
    verify(repository).findAll(PageRequest.of(0, 10, sort))
  }

  @Test
  fun `filters games by name`() {
    val page = PageImpl(listOf(TestFixtures.game()))
    whenever(repository.findAll(eq("mine"), any<PageRequest>())).thenReturn(page)

    assertSame(page, service.findGamesWithFilter(0, 10, "mine", sort))
    verify(repository).findAll("mine", PageRequest.of(0, 10, sort))
  }

  @Test
  fun `propagates game filter repository failure`() {
    whenever(repository.findAll(any<PageRequest>())).thenThrow(IllegalStateException("database"))

    assertThrows(IllegalStateException::class.java) {
      service.findGamesWithFilter(0, 10, null, sort)
    }
  }

  @Test
  fun `uploads game image and saves it`() {
    val image = TestFixtures.image()
    val command = GameCreateCommand(GameId("minecraft"), "Minecraft", null, image)
    whenever(repository.existsByIdIgnoreCase(command.id)).thenReturn(false)
    whenever(repository.save(any())).thenAnswer { it.arguments[0] }
    whenever(storage.uploadImage("Minecraft/logo.png", image)).thenReturn("stored/logo.png")

    val actual = service.uploadGame(command)

    assertEquals("stored/logo.png", actual.imagePath)
    verify(storage).uploadImage("Minecraft/logo.png", image)
    verify(repository, times(2)).save(any())
  }

  @Test
  fun `rejects duplicate game`() {
    val command = GameCreateCommand(GameId("minecraft"), "Minecraft", null, TestFixtures.image())
    whenever(repository.existsByIdIgnoreCase(command.id)).thenReturn(true)

    assertThrows(GameExistsException::class.java) { service.uploadGame(command) }
    verify(storage, never()).uploadImage(any(), any())
  }

  @Test
  fun `propagates image storage failure`() {
    val image = TestFixtures.image()
    val command = GameCreateCommand(GameId("minecraft"), "Minecraft", null, image)
    whenever(repository.existsByIdIgnoreCase(command.id)).thenReturn(false)
    whenever(repository.save(any())).thenAnswer { it.arguments[0] }
    whenever(storage.uploadImage(any(), any())).thenThrow(IllegalStateException("storage"))

    assertThrows(IllegalStateException::class.java) { service.uploadGame(command) }
  }

  @Test
  fun `deletes game`() {
    val id = GameId("minecraft")

    service.deleteGame(id)

    verify(repository).deleteById(id)
  }
}
