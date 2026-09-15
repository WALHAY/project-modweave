package git.walhay.modweave.api.user.repository

import git.walhay.modweave.api.user.User
import git.walhay.modweave.api.user.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@Tag("interaction")
class JpaUserRepositoryTest {
  private val springData = mock<SpringDataUserRepository>()
  private val repository = JpaUserRepository(springData)

  @Test
  fun `saves user and maps entity`() {
    val user = User("alice", "Alice", "alice@example.com", "password")
    whenever(springData.save(any<UserEntity>())).thenReturn(UserEntity.fromUser(user))

    assertEquals(user, repository.save(user))
    verify(springData).save<UserEntity>(any())
  }

  @Test
  fun `checks username and email existence`() {
    whenever(springData.existsByUsernameIgnoreCase("alice")).thenReturn(true)
    whenever(springData.existsByEmailIgnoreCase("alice@example.com")).thenReturn(true)

    assertTrue(repository.existsByUsername(UserId("alice")))
    assertTrue(repository.existsByEmail("alice@example.com"))
  }
}
