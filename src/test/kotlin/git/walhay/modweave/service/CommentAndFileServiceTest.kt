package git.walhay.modweave.service

import git.walhay.modweave.api.comment.*
import git.walhay.modweave.api.comment.command.CommentCreateCommand
import git.walhay.modweave.api.comment.exception.CommentNotFoundException
import git.walhay.modweave.api.comment.repository.CommentRepository
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.*
import git.walhay.modweave.testutils.InteractionTest
import git.walhay.modweave.testutils.TestFixtures
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

@InteractionTest
class CommentServiceTest {
  @Test
  fun `finds existing comment`() {
    val repository = mock<CommentRepository>()
    val users = mock<IUserService>()
    val service = CommentService(repository, users)
    val comment = TestFixtures.comment()
    whenever(repository.findById(comment.id)).thenReturn(comment)
    assertSame(comment, service.findCommentById(comment.id))
  }

  @Test
  fun `throws when comment is missing`() {
    val repository = mock<CommentRepository>()
    val users = mock<IUserService>()
    val service = CommentService(repository, users)
    val id = TestFixtures.comment().id
    whenever(repository.findById(id)).thenReturn(null)

    assertThrows(CommentNotFoundException::class.java) { service.findCommentById(id) }
  }

  @Test
  fun `creates comment for existing user`() {
    val repository = mock<CommentRepository>()
    val users = mock<IUserService>()
    val service = CommentService(repository, users)
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
  fun `propagates missing author when creating comment`() {
    val repository = mock<CommentRepository>()
    val users = mock<IUserService>()
    val service = CommentService(repository, users)
    val author = UserId("missing")
    whenever(users.findUserByUsername(author)).thenThrow(IllegalStateException("user missing"))

    assertThrows(IllegalStateException::class.java) {
      service.createComment(author, CommentCreateCommand("Useful", ModId("sodium")))
    }
    verify(repository, never()).save(any())
  }

  @Test
  fun `deletes comment`() {
    val repository = mock<CommentRepository>()
    val service = CommentService(repository, mock())
    val id = TestFixtures.comment().id

    service.deleteComment(UserId("alice"), id)

    verify(repository).delete(id)
  }

  @Test
  fun `propagates comment delete failure`() {
    val repository = mock<CommentRepository>()
    val service = CommentService(repository, mock())
    val id = TestFixtures.comment().id
    doThrow(IllegalStateException("database")).whenever(repository).delete(id)

    assertThrows(IllegalStateException::class.java) { service.deleteComment(UserId("alice"), id) }
  }
}
