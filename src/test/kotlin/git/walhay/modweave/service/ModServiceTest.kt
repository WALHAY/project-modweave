package git.walhay.modweave.service

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.category.repository.CategoryRepository
import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.game.*
import git.walhay.modweave.api.mod.*
import git.walhay.modweave.api.mod.command.ModCreateCommand
import git.walhay.modweave.api.mod.exception.*
import git.walhay.modweave.api.mod.repository.ModRepository
import git.walhay.modweave.api.storage.ISimpleStorageService
import git.walhay.modweave.api.user.*
import git.walhay.modweave.api.version.*
import git.walhay.modweave.testutils.TestFixtures
import java.util.UUID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class ModServiceTest : ServiceTestSupport() {
  private val repository = mock<ModRepository>()
  private val users = mock<IUserService>()
  private val categories = mock<CategoryRepository>()
  private val games = mock<IGameService>()
  private val versions = mock<IVersionService>()
  private val storage = mock<ISimpleStorageService>()
  private val service =
      ModService(repository, users, categories, games, versions, storage, pagePolicy)

  @Test
  fun `finds mod and throws when absent`() {
    val mod = TestFixtures.mod()
    whenever(repository.findById(mod.id)).thenReturn(mod)
    assertSame(mod, service.findModById(mod.id))

    whenever(repository.findById(ModId("missing"))).thenReturn(null)
    assertThrows(ModNotFoundException::class.java) { service.findModById(ModId("missing")) }
  }

  @Test
  fun `delegates mod filters`() {
    val page = PageImpl(listOf(TestFixtures.mod()))
    whenever(repository.findAll(any<Pageable>())).thenReturn(page)
    whenever(repository.findAll(eq("sod"), any<Pageable>())).thenReturn(page)
    whenever(repository.findAllByUser(any<UserId>(), any<Pageable>())).thenReturn(page)
    val collectionId = CollectionId(UUID.randomUUID())
    whenever(repository.findModsInCollection(any<CollectionId>(), any<Pageable>())).thenReturn(page)

    assertSame(page, service.findModsWithFilter(0, 10, null, sort))
    assertSame(page, service.findModsWithFilter(0, 10, "sod", sort))
    assertSame(page, service.findModsOfUser(UserId("alice"), 0, 10, sort))
    assertSame(page, service.findModsInCollection(collectionId, 0, 10, sort))
  }

  @Test
  fun `uploads mod and creates initial version`() {
    val command =
        ModCreateCommand(
            ModId("sodium"),
            "Sodium",
            "Performance",
            TestFixtures.image(),
            setOf(CategoryId("performance")),
            "1.0.0",
            listOf(TestFixtures.archive()),
            GameId("minecraft"))
    val user = TestFixtures.user()
    val game = TestFixtures.game()
    val category = TestFixtures.category("performance")
    whenever(repository.existsById(command.id)).thenReturn(false)
    whenever(users.findUserByUsername(UserId("alice"))).thenReturn(user)
    whenever(games.findGameById(command.gameId)).thenReturn(game)
    whenever(categories.findAllByNameIn(command.categories)).thenReturn(setOf(category))
    whenever(storage.uploadImage(any(), any())).thenReturn("stored/logo.png")
    whenever(repository.save(any())).thenAnswer { it.arguments[0] }

    val actual = service.uploadMod(UserId("alice"), command)

    assertEquals(ModId("sodium"), actual.id)
    verify(versions).createModVersion(actual, command)
  }

  @Test
  fun `rejects duplicate mod before side effects`() {
    val command =
        ModCreateCommand(
            ModId("sodium"),
            "Sodium",
            null,
            TestFixtures.image(),
            versionName = "1.0.0",
            gameId = GameId("minecraft"))
    whenever(repository.existsById(command.id)).thenReturn(true)

    assertThrows(ModExistsException::class.java) { service.uploadMod(UserId("alice"), command) }
    verify(users, never()).findUserByUsername(any())
    verify(storage, never()).uploadImage(any(), any())
  }

  @Test
  fun `removes image when version creation fails`() {
    val command =
        ModCreateCommand(
            ModId("sodium"),
            "Sodium",
            null,
            TestFixtures.image(),
            versionName = "1.0.0",
            gameId = GameId("minecraft"))
    whenever(repository.existsById(command.id)).thenReturn(false)
    whenever(users.findUserByUsername(any())).thenReturn(TestFixtures.user())
    whenever(games.findGameById(any())).thenReturn(TestFixtures.game())
    whenever(categories.findAllByNameIn(any())).thenReturn(emptySet())
    whenever(storage.uploadImage(any(), any())).thenReturn("stored/logo.png")
    whenever(repository.save(any())).thenAnswer { it.arguments[0] }
    doThrow(IllegalStateException("version failed"))
        .whenever(versions)
        .createModVersion(any<Mod>(), any<ModCreateCommand>())

    assertThrows(IllegalStateException::class.java) { service.uploadMod(UserId("alice"), command) }
    verify(storage).removeImage("Sodium/logo.png")
  }

  @Test
  fun `deletes mod`() {
    val id = ModId("sodium")

    service.deleteMod(UserId("alice"), id)

    verify(repository).deleteById(id)
  }
}
