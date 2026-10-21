package git.walhay.modweave.domain.security

import git.walhay.modweave.domain.collection.CollectionId
import git.walhay.modweave.domain.collection.CollectionRepository
import git.walhay.modweave.domain.comment.CommentId
import git.walhay.modweave.domain.comment.CommentRepository
import git.walhay.modweave.domain.mod.ModId
import git.walhay.modweave.domain.mod.ModRepository
import git.walhay.modweave.domain.user.UserId
import git.walhay.modweave.domain.version.VersionId
import git.walhay.modweave.domain.version.VersionRepository
import git.walhay.modweave.domain.version.VersionStatus
import java.util.UUID
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component("accessSecurity")
class AccessSecurity(
    private val modRepository: ModRepository,
    private val collectionRepository: CollectionRepository,
    private val commentRepository: CommentRepository,
    private val versionRepository: VersionRepository,
) {
  fun isAdmin(): Boolean = currentAuth()?.authorities?.any { it.authority == "ROLE_ADMIN" } == true

  fun isSelf(userId: String): Boolean = currentAuth()?.name == userId

  fun isSelfOrAdmin(userId: String): Boolean = isAdmin() || isSelf(userId)

  fun canManageMod(modId: String): Boolean =
      currentAuth()?.let { isModOwnerOrAdmin(it.name, modId) } == true

  fun canReadVersion(versionId: UUID): Boolean {
    val version = versionRepository.findVersionById(VersionId(versionId)) ?: return true
    return version.status == VersionStatus.APPROVED || canManageMod(version.modId.value)
  }

  fun isModOwnerOrAdmin(userId: String, modId: String): Boolean =
      isAdmin() ||
          (isSelf(userId) && modRepository.findById(ModId(modId))?.publisherId == UserId(userId))

  fun isCollectionOwnerOrAdmin(userId: String, collectionId: UUID): Boolean =
      isAdmin() ||
          (isSelf(userId) &&
              collectionRepository.findById(CollectionId(collectionId))?.owner == UserId(userId))

  fun isCommentOwnerOrAdmin(userId: String, commentId: UUID): Boolean =
      isAdmin() ||
          (isSelf(userId) &&
              commentRepository.findById(CommentId(commentId))?.authorId == UserId(userId))

  private fun currentAuth() =
      SecurityContextHolder.getContext().authentication?.takeIf {
        it.isAuthenticated && it !is AnonymousAuthenticationToken
      }
}
