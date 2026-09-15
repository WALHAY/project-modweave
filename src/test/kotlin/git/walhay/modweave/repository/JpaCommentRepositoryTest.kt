package git.walhay.modweave.api.comment.repository

import git.walhay.modweave.api.comment.Comment
import git.walhay.modweave.testutils.BoundaryConditionTest
import git.walhay.modweave.testutils.StateTransitionTest
import java.util.Optional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class JpaCommentRepositoryTest {
  private val springData = mock<SpringDataCommentRepository>()
  private val repository = JpaCommentRepository(springData)

  @BoundaryConditionTest
  @Test
  fun `returns missing comment as null`() {
    val comment = Comment()
    whenever(springData.findById(comment.id.value)).thenReturn(Optional.empty())

    assertNull(repository.findById(comment.id))
  }

  @StateTransitionTest
  @Test
  fun `saves comment and maps entity`() {
    val comment = Comment(content = "Useful")
    whenever(springData.save(any<CommentEntity>())).thenReturn(CommentEntity.fromComment(comment))

    assertEquals(comment, repository.save(comment))
    verify(springData).save(any<CommentEntity>())
  }
}
