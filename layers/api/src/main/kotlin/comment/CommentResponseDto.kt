package git.walhay.modweave.api.comment

import com.fasterxml.jackson.annotation.JsonIgnore
import git.walhay.modweave.api.common.ApiLink
import git.walhay.modweave.api.common.ApiLinkContext
import git.walhay.modweave.api.common.ApiResource
import git.walhay.modweave.domain.comment.Comment
import java.time.LocalDateTime
import java.util.UUID

data class CommentResponseDto(
    val id: UUID,
    val content: String,
    val publishDate: LocalDateTime,
    val authorId: String,
    @get:JsonIgnore val modId: String,
    override val links: Map<String, ApiLink> = emptyMap(),
) : ApiResource {
  override fun withLinks(context: ApiLinkContext): CommentResponseDto =
      with(context) {
        copy(
            links =
                buildMap {
                  put("self", link("/comments/{id}", id))
                  put("mod", link("/mods/{id}", modId))
                  put("author", link("/users/{id}", authorId))
                  if (canManage(authorId))
                      put("delete", link("/comments/{id}", id, method = "DELETE"))
                })
      }

  companion object {
    fun fromComment(comment: Comment): CommentResponseDto =
        CommentResponseDto(
            id = comment.id.value,
            content = comment.content,
            publishDate = comment.publishDate,
            authorId = comment.authorId.value,
            modId = comment.modId.value,
        )
  }
}
