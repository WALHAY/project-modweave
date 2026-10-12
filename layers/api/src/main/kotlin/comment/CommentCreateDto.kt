package git.walhay.modweave.api.comment

import git.walhay.modweave.domain.comment.CommentCreateCommand
import git.walhay.modweave.domain.mod.ModId
import jakarta.validation.constraints.NotBlank

data class CommentCreateDto(
    @field:NotBlank val content: String,
    @field:NotBlank val modId: String,
) {
  fun toCommentCreateCommand(): CommentCreateCommand = CommentCreateCommand(content, ModId(modId))
}
