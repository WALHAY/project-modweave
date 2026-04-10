package git.walhay.modweave.repository

import git.walhay.modweave.api.comment.Comment
import git.walhay.modweave.api.comment.repository.CommentRepository
import git.walhay.modweave.api.comment.repository.JpaCommentRepository
import git.walhay.modweave.api.game.Game
import git.walhay.modweave.api.game.repository.JpaGameRepository
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.repository.JpaModRepository
import git.walhay.modweave.api.user.User
import git.walhay.modweave.api.user.repository.JpaUserRepository
import git.walhay.modweave.api.user.repository.UserRepository
import git.walhay.modweave.testutils.PostgresTestTemplate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

@Import(JpaCommentRepository::class, JpaUserRepository::class, JpaGameRepository::class, JpaModRepository::class)
class CommentRepositoryTest : PostgresTestTemplate() {

  @Autowired lateinit var commentRepository: CommentRepository

  @Autowired lateinit var userRepository: UserRepository

  @Autowired lateinit var gameRepository: git.walhay.modweave.api.game.repository.GameRepository

  @Autowired lateinit var modRepository: git.walhay.modweave.api.mod.repository.ModRepository

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
                gameId = game.id))
    return author to mod
  }

  private fun seedComment(): Comment {
    val (author, mod) = seedModAndAuthor()
    return commentRepository.save(Comment(content = "hello", authorId = author.username, modId = mod.id))
  }

  @Test
  fun `create comment`() {
    val created = seedComment()
    assertNotNull(created)
    assertTrue(created.id.value > 0)
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
