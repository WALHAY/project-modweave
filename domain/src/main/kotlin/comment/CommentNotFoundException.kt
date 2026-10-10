package git.walhay.modweave.domain.comment

class CommentNotFoundException(
    id: CommentId,
) : RuntimeException("Comment with id '$id' not found")
