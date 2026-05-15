package git.walhay.modweave.api.comment.exception

import git.walhay.modweave.api.comment.CommentId

class CommentNotFoundException(
    id: CommentId,
) : Exception("Comment with id '$id' not found")
