package git.walhay.modweave.service

import git.walhay.modweave.api.comment.*
import git.walhay.modweave.api.comment.command.CommentCreateCommand
import git.walhay.modweave.api.comment.exception.CommentNotFoundException
import git.walhay.modweave.api.comment.repository.CommentRepository
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.*
import git.walhay.modweave.testutils.TestFixtures
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class CommentServiceTest {
  @Test
  fun `finds comment or throws`() {
    val repository = mock<CommentRepository>()
    val users = mock<IUserService>()
    val service = CommentService(repository, users)
    val comment = TestFixtures.comment()
    whenever(repository.findById(comment.id)).thenReturn(comment)
    assertSame(comment, service.findCommentById(comment.id))

    whenever(repository.findById(comment.id)).thenReturn(null)
    assertThrows(CommentNotFoundException::class.java) { service.findCommentById(comment.id) }
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
  fun `deletes comment`() {
    val repository = mock<CommentRepository>()
    val service = CommentService(repository, mock())
    val id = TestFixtures.comment().id

    service.deleteComment(UserId("alice"), id)

    verify(repository).delete(id)
  }
}
