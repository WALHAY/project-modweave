package git.walhay.modweave.repository

import git.walhay.modweave.domain.comment.Comment
import git.walhay.modweave.domain.comment.CommentRepository
import git.walhay.modweave.domain.game.Game
import git.walhay.modweave.domain.mod.Mod
import git.walhay.modweave.domain.mod.ModId
import git.walhay.modweave.domain.user.User
import git.walhay.modweave.domain.user.UserRepository
import git.walhay.modweave.persistence.comment.JpaCommentRepository
import git.walhay.modweave.persistence.game.JpaGameRepository
import git.walhay.modweave.persistence.mod.JpaModRepository
import git.walhay.modweave.persistence.user.JpaUserRepository
import git.walhay.modweave.testutils.PostgresTestTemplate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

@Import(
    JpaCommentRepository::class,
    JpaUserRepository::class,
    JpaGameRepository::class,
    JpaModRepository::class,
)
class CommentRepositoryTest : PostgresTestTemplate() {
  @Autowired lateinit var commentRepository: CommentRepository

  @Autowired lateinit var userRepository: UserRepository

  @Autowired lateinit var gameRepository: git.walhay.modweave.domain.game.GameRepository

  @Autowired lateinit var modRepository: git.walhay.modweave.domain.mod.ModRepository

  private fun seedModAndAuthor(): Pair<User, Mod> {
    val author = userRepository.save(User("author", "author", "author@mail.ru", "pass"))
    val game = gameRepository.save(Game(name = "Game", description = null, imagePath = "img.png"))
    val mod =
        modRepository.save(
            Mod(
                id = ModId("mod"),
                name = "Mod",
                description = null,
                imagePath = "img.png",
                publisherId = author.username,
                gameId = game.id,
            ),
        )
    return author to mod
  }

  private fun seedComment(): Comment {
    val (author, mod) = seedModAndAuthor()
    return commentRepository.save(
        Comment(content = "hello", authorId = author.username, modId = mod.id))
  }

  @Test
  fun `create comment`() {
    val created = seedComment()
    assertNotNull(created)
    assertNotEquals(java.util.UUID(0, 0), created.id.value)
  }

  @Test
  fun `read comment`() {
    val created = seedComment()

    val found = commentRepository.findById(created.id)
    assertNotNull(found)
    assertEquals(created.content, found!!.content)
  }

  @Test
  fun `update comment`() {
    val created = seedComment()

    commentRepository.save(created.copy(content = "updated"))

    val updated = commentRepository.findById(created.id)
    assertNotNull(updated)
    assertEquals("updated", updated!!.content)
  }

  @Test
  fun `delete comment`() {
    val created = seedComment()

    commentRepository.delete(created.id)

    val afterDelete = commentRepository.findById(created.id)
    assertNull(afterDelete)
  }
}
