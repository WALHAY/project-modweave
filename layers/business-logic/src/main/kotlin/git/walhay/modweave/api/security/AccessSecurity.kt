package git.walhay.modweave.api.security

import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.collection.repository.CollectionRepository
import git.walhay.modweave.api.comment.CommentId
import git.walhay.modweave.api.comment.repository.CommentRepository
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.repository.ModRepository
import git.walhay.modweave.api.user.UserId
import java.util.UUID
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component("accessSecurity")
class AccessSecurity(
    private val modRepository: ModRepository,
    private val collectionRepository: CollectionRepository,
    private val commentRepository: CommentRepository,
) {

  private fun isAdmin(authentication: Authentication? = currentAuth()): Boolean =
      authentication?.authorities?.any { it.authority == "ROLE_ADMIN" } == true

  fun isAdmin() = isAdmin(currentAuth())

  private fun isSelf(userId: UserId, authentication: Authentication?): Boolean =
      authentication?.name == userId.value

  fun isSelf(userId: String) = isSelf(UserId(userId), currentAuth())

  private fun isSelfOrAdmin(userId: UserId, authentication: Authentication?): Boolean =
      isAdmin(authentication) || isSelf(userId, authentication)

  fun isSelfOrAdmin(userId: String) = isSelfOrAdmin(UserId(userId), currentAuth())

  private fun isModOwnerOrAdmin(
      userId: UserId,
      modId: ModId,
      authentication: Authentication? = currentAuth(),
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

  fun isModOwnerOrAdmin(
      userId: String,
      modId: String,
  ) = isModOwnerOrAdmin(UserId(userId), ModId(modId), currentAuth())

  private fun isCollectionOwnerOrAdmin(
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

  fun isCollectionOwnerOrAdmin(userId: String, collectionId: UUID) =
      isCollectionOwnerOrAdmin(UserId(userId), CollectionId(collectionId), currentAuth())

  private fun isCommentOwnerOrAdmin(
      userId: UserId,
      commentId: CommentId,
      authentication: Authentication? = currentAuth(),
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

  fun isCommentOwnerOrAdmin(
      userId: String,
      commentId: UUID,
  ) = isCommentOwnerOrAdmin(UserId(userId), CommentId(commentId), currentAuth())

  private fun currentAuth(): Authentication? = SecurityContextHolder.getContext().authentication
}
