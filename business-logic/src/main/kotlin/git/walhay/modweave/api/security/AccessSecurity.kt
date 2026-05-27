package git.walhay.modweave.api.security

import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.collection.repository.CollectionRepository
import git.walhay.modweave.api.comment.CommentId
import git.walhay.modweave.api.comment.repository.CommentRepository
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.repository.ModRepository
import git.walhay.modweave.api.user.UserId
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.UUID

@Component("accessSecurity")
class AccessSecurity(
    private val modRepository: ModRepository,
    private val collectionRepository: CollectionRepository,
    private val commentRepository: CommentRepository,
) {

  fun isAdmin() = isAdmin(currentAuth())

  fun isAdmin(authentication: Authentication? = currentAuth()): Boolean =
      authentication?.authorities?.any { it.authority == "ROLE_ADMIN" } == true

  fun isSelf(userId: UserId, authentication: Authentication? = currentAuth()): Boolean =
      authentication?.name == userId.value

  fun isSelfOrAdmin(userId: UserId, authentication: Authentication? = currentAuth()): Boolean =
      isAdmin(authentication) || isSelf(userId, authentication)

  fun isModOwnerOrAdmin(
      userId: String,
      modId: String
  ) = isModOwnerOrAdmin(UserId(userId), ModId(modId), currentAuth())

  fun isModOwnerOrAdmin(
      userId: UserId,
      modId: ModId,
      authentication: Authentication?,
  ): Boolean {
    if (isAdmin(authentication)) {
      return true
    }
    if (!isSelf(userId, authentication)) {
      return false
    }
    val mod = modRepository.findById(modId) ?: return false
    return mod.publisherId == userId
  }

  fun isCollectionOwnerOrAdmin(userId: UserId, collectionId: CollectionId) = isCollectionOwnerOrAdmin(userId, collectionId, currentAuth())

  fun isCollectionOwnerOrAdmin(
      userId: UserId,
      collectionId: CollectionId,
      authentication: Authentication?
  ): Boolean {
    if (isAdmin(authentication)) {
      return true
    }
    if (!isSelf(userId, authentication)) {
      return false
    }
    val collection = collectionRepository.findById(collectionId) ?: return false
    return collection.owner == userId
  }

  fun isCommentOwnerOrAdmin(userId: String, commentId: Long) = isCommentOwnerOrAdmin(UserId(userId), CommentId(commentId), currentAuth())

  fun isCommentOwnerOrAdmin(
      userId: UserId,
      commentId: CommentId,
      authentication: Authentication?
  ): Boolean {
    if (isAdmin(authentication)) {
      return true
    }
    if (!isSelf(userId, authentication)) {
      return false
    }
    val comment = commentRepository.findById(commentId) ?: return false
    return comment.authorId == userId
  }

  private fun currentAuth(): Authentication? = SecurityContextHolder.getContext().authentication
}
