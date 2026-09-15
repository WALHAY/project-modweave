package git.walhay.modweave.service

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.category.CategoryService
import git.walhay.modweave.api.category.command.CategoryCreateCommand
import git.walhay.modweave.api.category.command.CategoryUpdateCommand
import git.walhay.modweave.api.category.exception.CategoryExistsException
import git.walhay.modweave.api.category.exception.CategoryNotFoundException
import git.walhay.modweave.api.category.repository.CategoryRepository as ModCategoryRepository
import git.walhay.modweave.api.category.repository.CategoryRepository
import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.collection.CollectionService
import git.walhay.modweave.api.collection.command.CollectionCreateCommand
import git.walhay.modweave.api.collection.exception.CollectionNotFoundException
import git.walhay.modweave.api.collection.repository.CollectionRepository
import git.walhay.modweave.api.comment.CommentService
import git.walhay.modweave.api.comment.command.CommentCreateCommand
import git.walhay.modweave.api.comment.exception.CommentNotFoundException
import git.walhay.modweave.api.comment.repository.CommentRepository
import git.walhay.modweave.api.common.paging.PageSizePolicy
import git.walhay.modweave.api.file.FileService
import git.walhay.modweave.api.file.IFileService
import git.walhay.modweave.api.file.repository.FileRepository
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.game.GameService
import git.walhay.modweave.api.game.IGameService
import git.walhay.modweave.api.game.command.GameCreateCommand
import git.walhay.modweave.api.game.exception.GameExistsException
import git.walhay.modweave.api.game.exception.GameNotFoundException
import git.walhay.modweave.api.game.repository.GameRepository
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.ModService
import git.walhay.modweave.api.mod.command.ModCreateCommand
import git.walhay.modweave.api.mod.exception.ModExistsException
import git.walhay.modweave.api.mod.exception.ModNotFoundException
import git.walhay.modweave.api.mod.repository.ModRepository
import git.walhay.modweave.api.security.AccessSecurity
import git.walhay.modweave.api.storage.ISimpleStorageService
import git.walhay.modweave.api.user.IUserService
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.user.UserService
import git.walhay.modweave.api.user.command.UserCreateCommand
import git.walhay.modweave.api.user.command.UserUpdateCommand
import git.walhay.modweave.api.user.exception.UserEmailExistsException
import git.walhay.modweave.api.user.exception.UserLoginExistsException
import git.walhay.modweave.api.user.exception.UserNotFoundException
import git.walhay.modweave.api.user.repository.UserRepository
import git.walhay.modweave.api.version.IVersionService
import git.walhay.modweave.api.version.VersionService
import git.walhay.modweave.api.version.VersionStatus
import git.walhay.modweave.api.version.command.VersionCreateCommand
import git.walhay.modweave.api.version.exception.VersionExistsException
import git.walhay.modweave.api.version.exception.VersionNotFoundException
import git.walhay.modweave.api.version.repository.VersionRepository
import git.walhay.modweave.config.properties.ModweaveProperties
import git.walhay.modweave.testutils.TestFixtures
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.password.PasswordEncoder

class BusinessLogicServiceTest {
  private val pagePolicy = PageSizePolicy(ModweaveProperties())
  private val sort = Sort.by("name")

  @Nested
  inner class CategoryServiceTests {
    private val repository = mock<CategoryRepository>()
    private val service = CategoryService(repository)

    @Test
    fun `gets categories from repository`() {
      val expected = listOf(TestFixtures.category())
      whenever(repository.findAll()).thenReturn(expected)

      val actual = service.getCategories()

      assertEquals(expected, actual)
      verify(repository).findAll()
    }

    @Test
    fun `rejects duplicate category`() {
      val command = CategoryCreateCommand(CategoryId("gameplay"), "Duplicate")
      whenever(repository.existsByNameIgnoreCase(command.name)).thenReturn(true)

      assertThrows(CategoryExistsException::class.java) { service.uploadCategory(command) }
      verify(repository, never()).save(any())
    }

    @Test
    fun `uploads category`() {
      val command = CategoryCreateCommand(CategoryId("performance"), "Performance")
      whenever(repository.existsByNameIgnoreCase(command.name)).thenReturn(false)
      whenever(repository.save(any())).thenAnswer { it.arguments[0] }

      val actual = service.uploadCategory(command)

      assertEquals(command.name, actual.name)
      verify(repository).save(any())
    }

    @Test
    fun `rejects update of missing category`() {
      val command = CategoryUpdateCommand(CategoryId("missing"), "Description")
      whenever(repository.existsByNameIgnoreCase(command.name)).thenReturn(false)

      assertThrows(CategoryNotFoundException::class.java) { service.updateCategory(command) }
      verify(repository, never()).save(any())
    }

    @Test
    fun `updates existing category`() {
      val command = CategoryUpdateCommand(CategoryId("gameplay"), "Updated")
      whenever(repository.existsByNameIgnoreCase(command.name)).thenReturn(true)
      whenever(repository.save(any())).thenAnswer { it.arguments[0] }

      assertEquals("Updated", service.updateCategory(command).description)
      verify(repository).save(any())
    }

    @Test
    fun `rejects deleting missing category`() {
      val id = CategoryId("missing")
      whenever(repository.existsByNameIgnoreCase(id)).thenReturn(false)

      assertThrows(CategoryNotFoundException::class.java) { service.deleteCategory(id) }
      verify(repository, never()).deleteByNameIgnoreCase(any())
    }

    @Test
    fun `deletes existing category`() {
      val id = CategoryId("gameplay")
      whenever(repository.existsByNameIgnoreCase(id)).thenReturn(true)

      service.deleteCategory(id)

      verify(repository).deleteByNameIgnoreCase(id)
    }
  }

  @Nested
  inner class UserServiceTests {
    private val repository = mock<UserRepository>()
    private val encoder = mock<PasswordEncoder>()
    private val service = UserService(repository, encoder, pagePolicy)

    @Test
    fun `finds user`() {
      val user = TestFixtures.user()
      whenever(repository.findByUsername(user.username)).thenReturn(user)

      assertSame(user, service.findUserByUsername(user.username))
    }

    @Test
    fun `throws when user is missing`() {
      val id = UserId("missing")
      whenever(repository.findByUsername(id)).thenReturn(null)

      assertThrows(UserNotFoundException::class.java) { service.findUserByUsername(id) }
    }

    @Test
    fun `filters users without a name`() {
      val page = PageImpl(listOf(TestFixtures.user()))
      whenever(repository.findAll(any<PageRequest>())).thenReturn(page)

      assertSame(page, service.findUsersWithFilter(0, 101, null, sort))
      verify(repository).findAll(PageRequest.of(0, 100, sort))
    }

    @Test
    fun `filters users by name`() {
      val page = PageImpl(listOf(TestFixtures.user()))
      whenever(repository.findAll(eq("ali"), any<PageRequest>())).thenReturn(page)

      assertSame(page, service.findUsersWithFilter(1, 10, "ali", sort))
      verify(repository).findAll("ali", PageRequest.of(1, 10, sort))
    }

    @Test
    fun `creates user with encoded password`() {
      val command = UserCreateCommand(UserId("alice"), "Alice", "plain", "alice@example.com")
      whenever(repository.existsByUsername(command.username)).thenReturn(false)
      whenever(repository.existsByEmail(command.email)).thenReturn(false)
      whenever(encoder.encode("plain")).thenReturn("encoded")
      whenever(repository.save(any())).thenAnswer { it.arguments[0] }

      val actual = service.createUser(command)

      assertEquals("encoded", actual.password)
      verify(encoder).encode("plain")
    }

    @Test
    fun `rejects duplicate username`() {
      val command = UserCreateCommand(UserId("alice"), "Alice", "plain", "alice@example.com")
      whenever(repository.existsByUsername(command.username)).thenReturn(true)

      assertThrows(UserLoginExistsException::class.java) { service.createUser(command) }
      verify(encoder, never()).encode(any())
    }

    @Test
    fun `rejects duplicate email`() {
      val command = UserCreateCommand(UserId("alice"), "Alice", "plain", "alice@example.com")
      whenever(repository.existsByUsername(command.username)).thenReturn(false)
      whenever(repository.existsByEmail(command.email)).thenReturn(true)

      assertThrows(UserEmailExistsException::class.java) { service.createUser(command) }
      verify(encoder, never()).encode(any())
    }

    @Test
    fun `updates user fields`() {
      val user = TestFixtures.user()
      whenever(repository.findByUsername(user.username)).thenReturn(user)
      whenever(encoder.encode("new-password")).thenReturn("new-encoded")
      whenever(repository.save(user)).thenReturn(user)

      val actual =
          service.updateUser(
              user.username,
              UserUpdateCommand(
                  name = "New name", password = "new-password", email = "new@example.com"))

      assertEquals("New name", actual.name)
      assertEquals("new-encoded", actual.password)
      assertEquals("new@example.com", actual.email)
      verify(repository).save(user)
    }

    @Test
    fun `rejects update with used email`() {
      val user = TestFixtures.user()
      whenever(repository.findByUsername(user.username)).thenReturn(user)
      whenever(repository.existsByEmail("used@example.com")).thenReturn(true)

      assertThrows(UserEmailExistsException::class.java) {
        service.updateUser(user.username, UserUpdateCommand(email = "used@example.com"))
      }
      verify(repository, never()).save(any())
    }
  }

  @Nested
  inner class GameServiceTests {
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
    fun `filters games with and without name`() {
      val page = PageImpl(listOf(TestFixtures.game()))
      whenever(repository.findAll(any<PageRequest>())).thenReturn(page)
      whenever(repository.findAll(eq("mine"), any<PageRequest>())).thenReturn(page)

      assertSame(page, service.findGamesWithFilter(0, 10, null, sort))
      assertSame(page, service.findGamesWithFilter(0, 10, "mine", sort))
      verify(repository).findAll(PageRequest.of(0, 10, sort))
      verify(repository).findAll("mine", PageRequest.of(0, 10, sort))
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
      verify(repository, org.mockito.kotlin.times(2)).save(any())
    }

    @Test
    fun `rejects duplicate game`() {
      val command = GameCreateCommand(GameId("minecraft"), "Minecraft", null, TestFixtures.image())
      whenever(repository.existsByIdIgnoreCase(command.id)).thenReturn(true)

      assertThrows(GameExistsException::class.java) { service.uploadGame(command) }
      verify(storage, never()).uploadImage(any(), any())
    }

    @Test
    fun `deletes game`() {
      val id = GameId("minecraft")

      service.deleteGame(id)

      verify(repository).deleteById(id)
    }
  }

  @Nested
  inner class ModServiceTests {
    private val repository = mock<ModRepository>()
    private val users = mock<IUserService>()
    private val categories = mock<ModCategoryRepository>()
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
      whenever(repository.findModsInCollection(any<CollectionId>(), any<Pageable>()))
          .thenReturn(page)

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

      assertThrows(IllegalStateException::class.java) {
        service.uploadMod(UserId("alice"), command)
      }
      verify(storage).removeImage("Sodium/logo.png")
    }

    @Test
    fun `deletes mod`() {
      val id = ModId("sodium")

      service.deleteMod(UserId("alice"), id)

      verify(repository).deleteById(id)
    }
  }

  @Nested
  inner class CollectionServiceTests {
    private val repository = mock<CollectionRepository>()
    private val mods = mock<IModService>()
    private val service = CollectionService(repository, pagePolicy, mods)

    @Test
    fun `gets collection or throws`() {
      val collection = TestFixtures.collection()
      whenever(repository.findById(collection.id)).thenReturn(collection)
      assertSame(collection, service.getCollectionById(collection.id))

      val missingId = CollectionId(UUID.randomUUID())
      whenever(repository.findById(missingId)).thenReturn(null)
      assertThrows(CollectionNotFoundException::class.java) { service.getCollectionById(missingId) }
    }

    @Test
    fun `creates collection`() {
      whenever(repository.save(any())).thenAnswer { it.arguments[0] }

      val result =
          service.createCollection(UserId("alice"), CollectionCreateCommand("Favorites", null))

      assertEquals("Favorites", result.name)
      assertEquals(UserId("alice"), result.owner)
      verify(repository).save(any())
    }

    @Test
    fun `adds mod to collection`() {
      val collection = TestFixtures.collection()
      val mod = TestFixtures.mod()
      whenever(repository.findById(collection.id)).thenReturn(collection)
      whenever(mods.findModById(mod.id)).thenReturn(mod)
      whenever(repository.save(collection)).thenReturn(collection)

      val result = service.addModToCollection(UserId("alice"), collection.id, mod.id, null)

      assertTrue(result.mods.contains(mod))
      verify(repository).save(collection)
    }

    @Test
    fun `finds collections of user`() {
      val page = PageImpl(listOf(TestFixtures.collection()))
      whenever(repository.findAllByUser(any<UserId>(), any<Pageable>())).thenReturn(page)

      assertSame(page, service.findCollectionsOfUser(UserId("alice"), 0, 10, sort))
      verify(repository).findAllByUser(any<UserId>(), any<Pageable>())
    }

    @Test
    fun `deletes collection`() {
      val id = CollectionId(UUID.randomUUID())

      service.deleteCollection(UserId("alice"), id)

      verify(repository).deleteById(id)
    }

    @Test
    fun `delete mod from collection is currently a no-op`() {
      assertDoesNotThrow {
        service.deleteModFromCollection(
            UserId("alice"), CollectionId(UUID.randomUUID()), ModId("sodium"))
      }
    }
  }

  @Nested
  inner class CommentServiceTests {
    private val repository = mock<CommentRepository>()
    private val users = mock<IUserService>()
    private val service = CommentService(repository, users)

    @Test
    fun `finds comment or throws`() {
      val comment = TestFixtures.comment()
      whenever(repository.findById(comment.id)).thenReturn(comment)
      assertSame(comment, service.findCommentById(comment.id))

      whenever(repository.findById(comment.id)).thenReturn(null)
      assertThrows(CommentNotFoundException::class.java) { service.findCommentById(comment.id) }
    }

    @Test
    fun `creates comment for existing user`() {
      val user = TestFixtures.user()
      val command = CommentCreateCommand("Useful", ModId("sodium"))
      whenever(users.findUserByUsername(user.username)).thenReturn(user)
      whenever(repository.save(any())).thenAnswer { it.arguments[0] }

      val actual = service.createComment(user.username, command)

      assertEquals(user.username, actual.authorId)
      assertEquals(command.modId, actual.modId)
      verify(repository).save(any())
    }

    @Test
    fun `deletes comment`() {
      val id = TestFixtures.comment().id

      service.deleteComment(UserId("alice"), id)

      verify(repository).delete(id)
    }
  }

  @Nested
  inner class FileServiceTests {
    private val storage = mock<ISimpleStorageService>()
    private val repository = mock<FileRepository>()
    private val service = FileService(storage, repository)

    @Test
    fun `uploads and saves every version file`() {
      val version = TestFixtures.version()
      val first = TestFixtures.archive("one.zip")
      val second = TestFixtures.archive("two.zip")
      whenever(storage.uploadVersionFile(any(), any())).thenAnswer { it.arguments[0] as String }
      whenever(repository.save(any())).thenAnswer { it.arguments[0] }

      service.uploadVersionFiles(version, listOf(first, second))

      assertEquals(2, version.files.size)
      verify(repository, org.mockito.kotlin.times(2)).save(any())
      verify(storage).uploadVersionFile("sodium/1.0.0/one.zip", first)
      verify(storage).uploadVersionFile("sodium/1.0.0/two.zip", second)
    }

    @Test
    fun `removes already uploaded files after failure`() {
      val version = TestFixtures.version()
      val first = TestFixtures.archive("one.zip")
      val second = TestFixtures.archive("two.zip")
      whenever(storage.uploadVersionFile("sodium/1.0.0/one.zip", first)).thenReturn("remote-one")
      whenever(storage.uploadVersionFile("sodium/1.0.0/two.zip", second))
          .thenThrow(IllegalStateException("upload failed"))
      whenever(repository.save(any())).thenAnswer { it.arguments[0] }

      assertThrows(IllegalStateException::class.java) {
        service.uploadVersionFiles(version, listOf(first, second))
      }
      verify(storage).removeVersionFile("remote-one")
    }
  }

  @Nested
  inner class VersionServiceTests {
    private val repository = mock<VersionRepository>()
    private val files = mock<IFileService>()
    private val security = mock<AccessSecurity>()
    private val service = VersionService(repository, files, security)
    private val modService: IModService = mock()

    init {
      val field = VersionService::class.java.getDeclaredField("modService")
      field.isAccessible = true
      field.set(service, modService)
    }

    @Test
    fun `gets version or throws`() {
      val version = TestFixtures.version()
      whenever(repository.findVersionById(version.id)).thenReturn(version)
      assertSame(version, service.getModVersion(version.id))

      whenever(repository.findVersionById(version.id)).thenReturn(null)
      assertThrows(VersionNotFoundException::class.java) { service.getModVersion(version.id) }
    }

    @Test
    fun `returns all versions for owner`() {
      val page = PageImpl(listOf(TestFixtures.version()))
      whenever(security.isModOwnerOrAdmin("alice", "sodium")).thenReturn(true)
      whenever(repository.findVersionsByModId(any(), any<Pageable>())).thenReturn(page)

      assertSame(
          page, service.getModVersions(UserId("alice"), ModId("sodium"), PageRequest.of(0, 10)))
      verify(repository).findVersionsByModId(ModId("sodium"), PageRequest.of(0, 10))
    }

    @Test
    fun `returns approved versions for another user`() {
      val page = PageImpl(listOf(TestFixtures.version()))
      whenever(security.isModOwnerOrAdmin("bob", "sodium")).thenReturn(false)
      whenever(repository.findVersionsByModIdAndStatus(any(), any(), any<Pageable>()))
          .thenReturn(page)

      assertSame(
          page, service.getModVersions(UserId("bob"), ModId("sodium"), PageRequest.of(0, 10)))
      verify(repository)
          .findVersionsByModIdAndStatus(
              ModId("sodium"), VersionStatus.APPROVED, PageRequest.of(0, 10))
    }

    @Test
    fun `creates version from mod and uploads files`() {
      val mod = TestFixtures.mod()
      val command =
          ModCreateCommand(
              mod.id,
              mod.name,
              null,
              TestFixtures.image(),
              versionName = "1.0.0",
              files = listOf(TestFixtures.archive()),
              gameId = mod.gameId)
      whenever(repository.save(any())).thenAnswer { it.arguments[0] }

      val result = service.createModVersion(mod, command)

      assertEquals("1.0.0", result.name)
      assertTrue(mod.versions.contains(result))
      verify(files).uploadVersionFiles(result, command.files)
    }

    @Test
    fun `creates version from id`() {
      val mod = TestFixtures.mod()
      val command =
          git.walhay.modweave.api.version.command.VersionCreateCommand(
              "2.0.0", "Changes", listOf(TestFixtures.archive()))
      whenever(modService.findModById(mod.id)).thenReturn(mod)
      whenever(repository.save(any())).thenAnswer { it.arguments[0] }

      val result = service.createModVersion(mod.id, command)

      assertEquals("2.0.0", result.name)
      verify(files).uploadVersionFiles(result, command.files)
    }

    @Test
    fun `rejects duplicate version name`() {
      val mod = TestFixtures.mod(versions = mutableListOf(TestFixtures.version("1.0.0")))
      val command =
          git.walhay.modweave.api.version.command.VersionCreateCommand("1.0.0", null, emptyList())
      whenever(modService.findModById(mod.id)).thenReturn(mod)

      assertThrows(VersionExistsException::class.java) { service.createModVersion(mod.id, command) }
      verify(repository, never()).save(any())
    }

    @Test
    fun `changes version status`() {
      val version = TestFixtures.version()
      whenever(repository.findVersionById(version.id)).thenReturn(version)
      whenever(repository.save(version)).thenReturn(version)

      val result = service.changeVersionStatus(UserId("admin"), version.id, VersionStatus.APPROVED)

      assertEquals(VersionStatus.APPROVED, result.status)
      verify(repository).save(version)
    }

    @Test
    fun `deletes version`() {
      val version = TestFixtures.version()
      whenever(repository.findVersionById(version.id)).thenReturn(version)

      service.deleteModVersion(version.id)

      verify(repository).delete(version.id)
    }
  }
}
