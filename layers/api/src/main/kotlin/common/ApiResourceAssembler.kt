package git.walhay.modweave.api.common

import git.walhay.modweave.api.category.CategoryCollectionDto
import git.walhay.modweave.api.category.CategoryController
import git.walhay.modweave.api.category.CategoryResponseDto
import git.walhay.modweave.api.collection.CollectionController
import git.walhay.modweave.api.collection.CollectionResponseDto
import git.walhay.modweave.api.comment.CommentController
import git.walhay.modweave.api.comment.CommentResponseDto
import git.walhay.modweave.api.file.FileController
import git.walhay.modweave.api.game.GameController
import git.walhay.modweave.api.game.GameResponseDto
import git.walhay.modweave.api.mod.ModController
import git.walhay.modweave.api.mod.ModResponseDto
import git.walhay.modweave.api.user.UserController
import git.walhay.modweave.api.user.UserResponseDto
import git.walhay.modweave.api.version.FileResponseDto
import git.walhay.modweave.api.version.VersionController
import git.walhay.modweave.api.version.VersionResponseDto
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class ApiResourceAssembler : RepresentationModelAssembler<ApiResource, ApiModel> {
  override fun toModel(entity: ApiResource): ApiModel {
    val auth =
        SecurityContextHolder.getContext().authentication?.takeIf {
          it.isAuthenticated && it !is AnonymousAuthenticationToken
        }
    val admin = auth?.authorities?.any { it.authority == "ROLE_ADMIN" } == true
    fun canManage(owner: String) = admin || auth?.name == owner

    val links = buildMap {
      when (entity) {
        is CategoryResponseDto -> {
          put("self", apiLink(CategoryController::getCategory, entity.name))
          put("collection", apiLink(CategoryController::listCategories))
          if (admin) {
            put("update", apiLink(CategoryController::patchCategory, entity.name))
            put("delete", apiLink(CategoryController::removeCategory, entity.name))
          }
        }
        is CategoryCollectionDto<*> -> {
          put("self", apiLink(CategoryController::listCategories))
          if (admin) put("create", apiLink(CategoryController::createCategory))
        }
        is UserResponseDto -> {
          put("self", apiLink(UserController::getUser, entity.username))
          put("mods", apiLink(UserController::getUserMods, entity.username, 0, 20))
          put("collections", apiLink(UserController::getUserCollections, entity.username, 0, 20))
          if (auth?.name == entity.username)
              put("update", apiLink(UserController::updateCurrentUser))
        }
        is GameResponseDto -> {
          put("self", apiLink(GameController::getGame, entity.id))
          put("mods", apiLink(ModController::getMods, 0, 20))
          if (admin) put("delete", apiLink(GameController::removeGame, entity.id))
        }
        is ModResponseDto -> {
          put("self", apiLink(ModController::getMod, entity.id))
          put("game", apiLink(GameController::getGame, entity.gameId))
          put("publisher", apiLink(UserController::getUser, entity.publisherId))
          put("versions", apiLink(ModController::getModVersions, entity.id, 0, 20))
          put("comments", apiLink(CommentController::getComments, entity.id, 0, 20))
          if (canManage(entity.publisherId)) {
            put("create-version", apiLink(VersionController::createVersion, entity.id))
            put("delete", apiLink(ModController::removeMod, entity.id))
          }
        }
        is VersionResponseDto<*> -> {
          val self = apiLink(VersionController::getVersion, entity.modId, entity.id)
          put("self", self)
          put("mod", apiLink(ModController::getMod, entity.modId))
          put("files", self)
          if (admin) {
            put("moderate", apiLink(VersionController::moderateVersion, entity.modId, entity.id))
            put("delete", apiLink(VersionController::removeVersion, entity.modId, entity.id))
          }
        }
        is FileResponseDto -> {
          val self = apiLink(FileController::getFile, entity.id)
          put("self", self)
          put("download", self)
        }
        is CollectionResponseDto -> {
          put("self", apiLink(CollectionController::getCollection, entity.id))
          put("mods", apiLink(CollectionController::getModsInCollection, entity.id, 0, 20))
          if (canManage(entity.ownerId)) {
            put("add-mod", apiLink(CollectionController::insertMod, entity.id))
            put("delete", apiLink(CollectionController::removeCollection, entity.id))
          }
        }
        is CommentResponseDto -> {
          put("self", apiLink(CommentController::getComment, entity.id))
          put("mod", apiLink(ModController::getMod, entity.modId))
          put("author", apiLink(UserController::getUser, entity.authorId))
          if (canManage(entity.authorId))
              put("delete", apiLink(CommentController::removeComment, entity.id))
        }
        else -> error("No representation for ${entity.javaClass.simpleName}")
      }
    }
    val payload =
        when (entity) {
          is CategoryCollectionDto<*> ->
              CategoryCollectionDto(entity.items.map { toModel(it as ApiResource) })
          is VersionResponseDto<*> ->
              VersionResponseDto(
                  entity.id,
                  entity.files.map { toModel(it as ApiResource) },
                  entity.name,
                  entity.changes,
                  entity.uploadDate,
                  entity.status,
                  entity.modId)
          else -> entity
        }
    return ApiModel(payload, links)
  }
}
