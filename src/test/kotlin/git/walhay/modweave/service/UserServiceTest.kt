package git.walhay.modweave.service

import git.walhay.modweave.api.user.*
import git.walhay.modweave.api.user.command.*
import git.walhay.modweave.api.user.exception.*
import git.walhay.modweave.api.user.repository.UserRepository
import git.walhay.modweave.testutils.TestFixtures
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.password.PasswordEncoder

class UserServiceTest : ServiceTestSupport() {
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
