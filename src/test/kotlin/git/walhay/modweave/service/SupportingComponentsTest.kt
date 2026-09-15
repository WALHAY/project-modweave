package git.walhay.modweave.service

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.category.http.dto.CategoryUpdateDto
import git.walhay.modweave.api.category.http.dto.CategoryUploadDto
import git.walhay.modweave.api.collection.http.dto.CollectionCreateDto
import git.walhay.modweave.api.collection.repository.CollectionRepository
import git.walhay.modweave.api.comment.http.dto.CommentCreateDto
import git.walhay.modweave.api.comment.repository.CommentRepository
import git.walhay.modweave.api.common.paging.PageRequest
import git.walhay.modweave.api.common.sorting.Sort
import git.walhay.modweave.api.game.http.dto.GameUploadDto
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.repository.ModRepository
import git.walhay.modweave.api.security.AccessSecurity
import git.walhay.modweave.api.user.http.dto.UserCreateDto
import git.walhay.modweave.api.user.http.dto.UserUpdateDto
import git.walhay.modweave.api.version.http.dto.VersionUploadDto
import git.walhay.modweave.config.JwtProperties
import git.walhay.modweave.config.JwtService
import git.walhay.modweave.testutils.BoundaryConditionTest
import git.walhay.modweave.testutils.TestFixtures
import git.walhay.modweave.util.ImageExtensionValidator
import git.walhay.modweave.util.spinalCase
import java.time.Duration
import java.util.Base64
import java.util.UUID
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User

@BoundaryConditionTest
class SupportingComponentsTest {
  @AfterEach
  fun clearSecurityContext() {
    SecurityContextHolder.clearContext()
  }

  @Test
  fun `page request rejects negative page`() {
    assertThrows(IllegalArgumentException::class.java) { PageRequest(page = -1) }
  }

  @Test
  fun `page request rejects non-positive size`() {
    assertThrows(IllegalArgumentException::class.java) { PageRequest(size = 0) }
  }

  @Test
  fun `page request converts page and size`() {
    val pageable = PageRequest(page = 2, size = 5).toSpringPageable()

    assertEquals(2, pageable.pageNumber)
    assertEquals(5, pageable.pageSize)
  }

  @Test
  fun `page request converts sort`() {
    val sort = Sort()
    val entriesField = Sort::class.java.getDeclaredField("entries")
    entriesField.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    (entriesField.get(sort) as MutableList<Sort.SortEntry>).addAll(
        listOf(
            Sort.SortEntry("name", Sort.Direction.ASC),
            Sort.SortEntry("createdAt", Sort.Direction.DESC)))

    val orders = PageRequest(sort = sort).toSpringPageable().sort.toList()

    assertEquals(listOf("name", "createdAt"), orders.map { it.property })
    assertEquals(listOf("ASC", "DESC"), orders.map { it.direction.name })
  }

  @Test
  fun `spinal case normalizes spaces and punctuation`() {
    assertEquals("my-game-2", "My Game 2".spinalCase())
    assertEquals("game", "Game!".spinalCase())
    assertEquals("", "!!!".spinalCase())
  }

  @Test
  fun `image validator accepts supported extensions case insensitively`() {
    val validator = ImageExtensionValidator()

    assertTrue(validator.isValid(TestFixtures.image("logo.PNG"), null))
    assertTrue(validator.isValid(TestFixtures.image("logo.jpeg"), null))
    assertFalse(validator.isValid(TestFixtures.image("logo.gif"), null))
    assertFalse(validator.isValid(null, null))
  }

  @Test
  fun `request DTOs convert to commands`() {
    val category = CategoryUploadDto("gameplay", "Description").toCategoryCreateCommand()
    val updated = CategoryUpdateDto("gameplay", null).toCategoryUpdateCommand()
    val user =
        UserCreateDto("alice", "Alice", "password", "alice@example.com").toUserCreateCommand()
    val collection = CollectionCreateDto("Favorites", null).toCollectionCreateCommand()
    val comment = CommentCreateDto("Useful", "sodium").toCommentCreateCommand()
    val version =
        VersionUploadDto("1.0.0", "Changes", listOf(TestFixtures.archive()))
            .toVersionCreateCommand()

    assertEquals(CategoryId("gameplay"), category.name)
    assertEquals(null, updated.description)
    assertEquals("alice", user.username.value)
    assertEquals("Favorites", collection.name)
    assertEquals(ModId("sodium"), comment.modId)
    assertEquals("1.0.0", version.name)
  }

  @Test
  fun `user update DTO rejects empty update`() {
    assertThrows(Exception::class.java) { UserUpdateDto() }
  }

  @Test
  fun `user update DTO converts email`() {
    assertEquals(
        "new@example.com", UserUpdateDto(email = "new@example.com").toUserUpdateCommand().email)
  }

  @Test
  fun `game upload DTO creates spinal case id`() {
    val command = GameUploadDto("My Cool Game", null, TestFixtures.image()).toGameCreateCommand()

    assertEquals("my-cool-game", command.id.value)
    assertEquals("My Cool Game", command.name)
  }

  @Test
  fun `jwt service extracts username and roles from access token`() {
    val jwt =
        JwtService(
            JwtProperties(
                secret = Base64.getEncoder().encodeToString(ByteArray(32) { 7 }),
                issuer = "modweave-test",
                accessTokenTtl = Duration.ofMinutes(5),
                refreshTokenTtl = Duration.ofMinutes(10)))
    val user = User.withUsername("alice").password("password").roles("USER", "ADMIN").build()

    val access = jwt.generateAccessToken(user)

    assertEquals("alice", jwt.extractUsername(access))
    assertEquals(setOf("ROLE_USER", "ROLE_ADMIN"), jwt.extractRoles(access).toSet())
  }

  @Test
  fun `jwt service validates token type and username`() {
    val jwt =
        JwtService(
            JwtProperties(
                secret = Base64.getEncoder().encodeToString(ByteArray(32) { 7 }),
                issuer = "modweave-test",
                accessTokenTtl = Duration.ofMinutes(5),
                refreshTokenTtl = Duration.ofMinutes(10)))
    val user = User.withUsername("alice").password("password").roles("USER", "ADMIN").build()
    val access = jwt.generateAccessToken(user)
    val refresh = jwt.generateRefreshToken(user)

    assertTrue(jwt.isAccessTokenValid(access, user))
    assertFalse(jwt.isRefreshTokenValid(access, user))
    assertTrue(jwt.isRefreshTokenValid(refresh, user))
    assertFalse(
        jwt.isAccessTokenValid(refresh, User.withUsername("bob").password("password").build()))
  }

  @Test
  fun `access security allows authenticated owner`() {
    val mods = mock<ModRepository>()
    val collections = mock<CollectionRepository>()
    val comments = mock<CommentRepository>()
    val security = AccessSecurity(mods, collections, comments)
    val collectionId = UUID.randomUUID()
    val commentId = UUID.randomUUID()

    SecurityContextHolder.getContext().authentication =
        TestingAuthenticationToken("alice", "credentials", "ROLE_USER")
    whenever(mods.findById(any())).thenReturn(TestFixtures.mod())
    whenever(collections.findById(any())).thenReturn(TestFixtures.collection())
    whenever(comments.findById(any())).thenReturn(TestFixtures.comment())

    assertTrue(security.isSelf("alice"))
    assertTrue(security.isSelfOrAdmin("alice"))
    assertTrue(security.isModOwnerOrAdmin("alice", "sodium"))
    assertTrue(security.isCollectionOwnerOrAdmin("alice", collectionId))
    assertTrue(security.isCommentOwnerOrAdmin("alice", commentId))
    assertFalse(security.isAdmin())
  }

  @Test
  fun `access security allows administrator`() {
    val mods = mock<ModRepository>()
    val collections = mock<CollectionRepository>()
    val comments = mock<CommentRepository>()
    val security = AccessSecurity(mods, collections, comments)
    val collectionId = UUID.randomUUID()
    val commentId = UUID.randomUUID()

    SecurityContextHolder.getContext().authentication =
        TestingAuthenticationToken("admin", "credentials", "ROLE_ADMIN")
    assertTrue(security.isAdmin())
    assertTrue(security.isSelfOrAdmin("anyone"))
    assertTrue(security.isModOwnerOrAdmin("anyone", "missing"))
    assertTrue(security.isCollectionOwnerOrAdmin("anyone", collectionId))
    assertTrue(security.isCommentOwnerOrAdmin("anyone", commentId))
  }

  @Test
  fun `access security denies missing resources`() {
    val mods = mock<ModRepository>()
    val collections = mock<CollectionRepository>()
    val comments = mock<CommentRepository>()
    val security = AccessSecurity(mods, collections, comments)
    val collectionId = UUID.randomUUID()
    val commentId = UUID.randomUUID()
    SecurityContextHolder.getContext().authentication =
        TestingAuthenticationToken("alice", "credentials", "ROLE_USER")
    whenever(mods.findById(any())).thenReturn(null)
    whenever(collections.findById(any())).thenReturn(null)
    whenever(comments.findById(any())).thenReturn(null)
    assertFalse(security.isModOwnerOrAdmin("alice", "missing"))
    assertFalse(security.isCollectionOwnerOrAdmin("alice", collectionId))
    assertFalse(security.isCommentOwnerOrAdmin("alice", commentId))
  }

  @Test
  fun `access security denies anonymous authentication`() {
    val security = AccessSecurity(mock(), mock(), mock())
    SecurityContextHolder.clearContext()

    assertFalse(security.isAdmin())
    assertFalse(security.isSelf("alice"))
    assertFalse(security.isSelfOrAdmin("alice"))
    assertFalse(security.isModOwnerOrAdmin("alice", "sodium"))
  }
}
