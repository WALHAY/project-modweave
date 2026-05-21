package git.walhay.modweave.cli

import git.walhay.modweave.api.comment.CommentId
import git.walhay.modweave.api.comment.ICommentService
import git.walhay.modweave.api.comment.command.CommentCreateCommand
import git.walhay.modweave.api.comment.http.dto.CommentResponseDto
import git.walhay.modweave.api.comment.http.dto.fromComment
import git.walhay.modweave.api.mod.ModId
import org.springframework.shell.core.command.annotation.Command
import org.springframework.shell.core.command.annotation.Option
import org.springframework.stereotype.Component

@Component
class CommentShellCommands(
    private val commentService: ICommentService,
    private val authSession: CliAuthSession,
) : ShellCommandSupport() {
  @Command(name = ["comment", "get"], description = "Get comment by id.")
  fun commentGet(
      @Option(longName = "id") id: Long,
  ): Any? =
      renderNullable(commentService.findCommentById(CommentId(id))?.let { CommentResponseDto.fromComment(it) })

  @Command(name = ["comment", "create"], description = "Create comment.")
  fun commentCreate(
      @Option(longName = "user-id", required = false) userId: String?,
      @Option(longName = "mod-id") modId: String,
      @Option(longName = "content") content: String,
  ): Any =
      renderValue(
          CommentResponseDto.fromComment(
              commentService.createComment(
                  authSession.resolveUserId(userId),
                  CommentCreateCommand(content, ModId(modId)),
              ),
          ),
      )

  @Command(name = ["comment", "delete"], description = "Delete comment.")
  fun commentDelete(
      @Option(longName = "user-id", required = false) userId: String?,
      @Option(longName = "id") id: Long,
  ): String {
    commentService.deleteComment(authSession.resolveUserId(userId), CommentId(id))
    return "Comment '$id' deleted."
  }
}
