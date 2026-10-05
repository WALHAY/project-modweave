package git.walhay.modweave.api.comment.http.dto

import git.walhay.modweave.api.comment.command.CommentCreateCommand
import git.walhay.modweave.api.mod.ModId
import jakarta.validation.constraints.NotBlank

data class CommentCreateDto(
    @field:NotBlank val content: String,
    @field:NotBlank val modId: String,
) {
  fun toCommentCreateCommand(): CommentCreateCommand = CommentCreateCommand(content, ModId(modId))
}
