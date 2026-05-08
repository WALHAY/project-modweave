package git.walhay.modweave.cli

import git.walhay.modweave.api.comment.CommentId
import git.walhay.modweave.api.comment.ICommentService
import git.walhay.modweave.api.comment.command.CommentCreateCommand
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import org.springframework.shell.core.command.annotation.Command
import org.springframework.shell.core.command.annotation.Option
import org.springframework.stereotype.Component

@Component
class CommentShellCommands(
    private val commentService: ICommentService,
) : ShellCommandSupport() {
  @Command(name = ["comments", "get"], description = "Get comment by id.")
  fun commentsGet(
      @Option(longName = "id") id: Long,
  ): Any? = renderNullable(commentService.findCommentById(CommentId(id)))

  @Command(name = ["comments", "create"], description = "Create comment.")
  fun commentsCreate(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "mod-id") modId: String,
      @Option(longName = "content") content: String,
  ): Any =
      renderValue(commentService.createComment(UserId(userId), CommentCreateCommand(content, ModId(modId))))

  @Command(name = ["comments", "delete"], description = "Delete comment.")
  fun commentsDelete(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "id") id: Long,
  ): String {
    commentService.deleteComment(UserId(userId), CommentId(id))
    return "Comment '$id' deleted."
  }
}
