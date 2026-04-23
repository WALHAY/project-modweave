package git.walhay.modweave.api.comment.http.dto

import git.walhay.modweave.api.comment.command.CommentCreateCommand
import io.mcarle.konvert.api.KonvertTo

@KonvertTo(CommentCreateCommand::class)
data class CommentCreateDto(
    val content: String,
    val modId: String,
)
