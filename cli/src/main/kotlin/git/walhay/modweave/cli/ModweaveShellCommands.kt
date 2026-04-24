package git.walhay.modweave.cli

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.category.ICategoryService
import git.walhay.modweave.api.category.command.CategoryCreateCommand
import git.walhay.modweave.api.category.command.CategoryUpdateCommand
import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.collection.ICollectionService
import git.walhay.modweave.api.collection.command.CollectionCreateCommand
import git.walhay.modweave.api.comment.CommentId
import git.walhay.modweave.api.comment.ICommentService
import git.walhay.modweave.api.comment.command.CommentCreateCommand
import git.walhay.modweave.api.file.IFileService
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.game.IGameService
import git.walhay.modweave.api.game.command.GameCreateCommand
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.mod.command.ModCreateCommand
import git.walhay.modweave.api.user.IUserService
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.user.command.UserCreateCommand
import git.walhay.modweave.api.user.command.UserUpdateCommand
import git.walhay.modweave.api.version.IVersionService
import git.walhay.modweave.api.version.VersionId
import git.walhay.modweave.api.version.command.VersionCreateCommand
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.shell.core.command.annotation.Command
import org.springframework.shell.core.command.annotation.Option
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class ModweaveShellCommands(
    private val categoryService: ICategoryService,
    private val gameService: IGameService,
    private val userService: IUserService,
    private val modService: IModService,
    private val versionService: IVersionService,
    private val collectionService: ICollectionService,
    private val commentService: ICommentService,
    private val fileService: IFileService,
) {
  @Command(name = ["categories", "list"], description = "List all categories.")
  fun categoriesList(): Any = categoryService.getCategories()

  @Command(name = ["categories", "create"], description = "Create a category.")
  fun categoriesCreate(
      @Option(longName = "name") name: String,
      @Option(longName = "description", required = false) description: String?,
  ): Any = categoryService.uploadCategory(CategoryCreateCommand(CategoryId(name), description))

  @Command(name = ["categories", "update"], description = "Update a category.")
  fun categoriesUpdate(
      @Option(longName = "name") name: String,
      @Option(longName = "description", required = false) description: String?,
  ): Any = categoryService.updateCategory(CategoryUpdateCommand(CategoryId(name), description))

  @Command(name = ["categories", "delete"], description = "Delete a category.")
  fun categoriesDelete(
      @Option(longName = "name") name: String,
  ): String {
    categoryService.deleteCategory(CategoryId(name))
    return "Category '$name' deleted."
  }

  @Command(name = ["games", "list"], description = "List games with paging.")
  fun gamesList(
      @Option(longName = "page", defaultValue = "0") page: Int,
      @Option(longName = "size", defaultValue = "20") size: Int,
      @Option(longName = "name", required = false) name: String?,
      @Option(longName = "sort", defaultValue = "name,asc") sort: String,
  ): Any = gameService.findGamesWithFilter(page, size, name, parseSort(sort))

  @Command(name = ["games", "get"], description = "Get game by id.")
  fun gamesGet(
      @Option(longName = "id") id: String,
  ): Any = gameService.findGameById(GameId(id))

  @Command(name = ["games", "create"], description = "Create a game.")
  fun gamesCreate(
      @Option(longName = "id") id: String,
      @Option(longName = "name") name: String,
      @Option(longName = "description", required = false) description: String?,
      @Option(longName = "image") imagePath: String,
  ): Any =
      gameService.uploadGame(
          GameCreateCommand(
              id = GameId(id),
              name = name,
              description = description,
              image = multipartFile(Path.of(imagePath)),
          ),
      )

  @Command(name = ["games", "delete"], description = "Delete game by id.")
  fun gamesDelete(
      @Option(longName = "id") id: String,
  ): String {
    gameService.deleteGame(GameId(id))
    return "Game '$id' deleted."
  }

  @Command(name = ["users", "list"], description = "List users with paging.")
  fun usersList(
      @Option(longName = "page", defaultValue = "0") page: Int,
      @Option(longName = "size", defaultValue = "20") size: Int,
      @Option(longName = "name", required = false) name: String?,
      @Option(longName = "sort", defaultValue = "username,asc") sort: String,
  ): Any = userService.findUsersWithFilter(page, size, name, parseSort(sort))

  @Command(name = ["users", "get"], description = "Get user by username.")
  fun usersGet(
      @Option(longName = "id") id: String,
  ): Any = userService.findUserByUsername(UserId(id))

  @Command(name = ["users", "create"], description = "Create user.")
  fun usersCreate(
      @Option(longName = "username") username: String,
      @Option(longName = "name") name: String,
      @Option(longName = "password") password: String,
      @Option(longName = "email") email: String,
  ): Any =
      userService.createUser(
          UserCreateCommand(
              username = UserId(username),
              name = name,
              password = password,
              email = email,
          ),
      )

  @Command(name = ["users", "update"], description = "Update user.")
  fun usersUpdate(
      @Option(longName = "id") id: String,
      @Option(longName = "name", required = false) name: String?,
      @Option(longName = "password", required = false) password: String?,
      @Option(longName = "email", required = false) email: String?,
  ): Any = userService.updateUser(UserId(id), UserUpdateCommand(name, password, email))

  @Command(name = ["mods", "list"], description = "List mods with paging.")
  fun modsList(
      @Option(longName = "page", defaultValue = "0") page: Int,
      @Option(longName = "size", defaultValue = "20") size: Int,
      @Option(longName = "name", required = false) name: String?,
      @Option(longName = "sort", defaultValue = "name,asc") sort: String,
  ): Any = modService.findModsWithFilter(page, size, name, parseSort(sort))

  @Command(name = ["mods", "get"], description = "Get mod by id.")
  fun modsGet(
      @Option(longName = "id") id: String,
  ): Any = modService.findModById(ModId(id))

  @Command(name = ["mods", "user"], description = "List mods for user.")
  fun modsUser(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "page", defaultValue = "0") page: Int,
      @Option(longName = "size", defaultValue = "20") size: Int,
      @Option(longName = "sort", defaultValue = "name,asc") sort: String,
  ): Any = modService.findModsOfUser(UserId(userId), page, size, parseSort(sort))

  @Command(name = ["mods", "collection"], description = "List mods in collection.")
  fun modsCollection(
      @Option(longName = "collection-id") collectionId: Long,
      @Option(longName = "page", defaultValue = "0") page: Int,
      @Option(longName = "size", defaultValue = "20") size: Int,
      @Option(longName = "sort", defaultValue = "name,asc") sort: String,
  ): Any = modService.findModsInCollection(CollectionId(collectionId), page, size, parseSort(sort))

  @Command(name = ["mods", "create"], description = "Create mod.")
  fun modsCreate(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "id") id: String,
      @Option(longName = "name") name: String,
      @Option(longName = "description", required = false) description: String?,
      @Option(longName = "image") imagePath: String,
      @Option(longName = "version-name") versionName: String,
      @Option(longName = "game-id") gameId: String,
      @Option(longName = "categories", required = false) categories: String?,
      @Option(longName = "files") files: String,
  ): Any =
      modService.uploadMod(
          UserId(userId),
          ModCreateCommand(
              id = ModId(id),
              name = name,
              description = description,
              image = multipartFile(Path.of(imagePath)),
              categories = categoryIds(categories),
              versionName = versionName,
              files = multipartFiles(files),
              gameId = GameId(gameId),
          ),
      )

  @Command(name = ["mods", "delete"], description = "Delete mod.")
  fun modsDelete(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "id") id: String,
  ): String {
    modService.deleteMod(UserId(userId), ModId(id))
    return "Mod '$id' deleted."
  }

  @Command(name = ["versions", "get"], description = "Get version by id.")
  fun versionsGet(
      @Option(longName = "id") id: Long,
  ): Any = versionService.getModVersion(VersionId(id))

  @Command(name = ["versions", "list"], description = "List versions of a mod.")
  fun versionsList(
      @Option(longName = "mod-id") modId: String,
      @Option(longName = "page", defaultValue = "0") page: Int,
      @Option(longName = "size", defaultValue = "20") size: Int,
      @Option(longName = "sort", defaultValue = "id,desc") sort: String,
  ): Any = versionService.getModVersions(ModId(modId), PageRequest.of(page, size, parseSort(sort)))

  @Command(name = ["versions", "create"], description = "Create mod version.")
  fun versionsCreate(
      @Option(longName = "mod-id") modId: String,
      @Option(longName = "name") name: String,
      @Option(longName = "changes", required = false) changes: String?,
      @Option(longName = "files") files: String,
  ): Any =
      versionService.createModVersion(
          ModId(modId),
          VersionCreateCommand(name, changes, multipartFiles(files)),
      )

  @Command(name = ["versions", "delete"], description = "Delete version.")
  fun versionsDelete(
      @Option(longName = "id") id: Long,
  ): String {
    versionService.deleteModVersion(VersionId(id))
    return "Version '$id' deleted."
  }

  @Command(name = ["collections", "get"], description = "Get collection by id.")
  fun collectionsGet(
      @Option(longName = "id") id: Long,
  ): Any = collectionService.getCollectionById(CollectionId(id))

  @Command(name = ["collections", "create"], description = "Create collection.")
  fun collectionsCreate(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "name") name: String,
      @Option(longName = "description", required = false) description: String?,
  ): Any = collectionService.createCollection(UserId(userId), CollectionCreateCommand(name, description))

  @Command(name = ["collections", "delete"], description = "Delete collection.")
  fun collectionsDelete(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "id") id: Long,
  ): String {
    collectionService.deleteCollection(UserId(userId), CollectionId(id))
    return "Collection '$id' deleted."
  }

  @Command(name = ["collections", "add-mod"], description = "Add mod to collection.")
  fun collectionsAddMod(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "collection-id") collectionId: Long,
      @Option(longName = "mod-id") modId: String,
      @Option(longName = "index", required = false) index: Int?,
  ): Any =
      collectionService.addModToCollection(
          UserId(userId),
          CollectionId(collectionId),
          ModId(modId),
          index,
      )

  @Command(name = ["collections", "delete-mod"], description = "Delete mod from collection.")
  fun collectionsDeleteMod(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "collection-id") collectionId: Long,
      @Option(longName = "mod-id") modId: String,
  ): String {
    collectionService.deleteModFromCollection(UserId(userId), CollectionId(collectionId), ModId(modId))
    return "Mod '$modId' deleted from collection '$collectionId'."
  }

  @Command(name = ["comments", "get"], description = "Get comment by id.")
  fun commentsGet(
      @Option(longName = "id") id: Long,
  ): Any? = commentService.findCommentById(CommentId(id))

  @Command(name = ["comments", "create"], description = "Create comment.")
  fun commentsCreate(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "mod-id") modId: String,
      @Option(longName = "content") content: String,
  ): Any = commentService.createComment(UserId(userId), CommentCreateCommand(content, ModId(modId)))

  @Command(name = ["comments", "delete"], description = "Delete comment.")
  fun commentsDelete(
      @Option(longName = "user-id") userId: String,
      @Option(longName = "id") id: Long,
  ): String {
    commentService.deleteComment(UserId(userId), CommentId(id))
    return "Comment '$id' deleted."
  }

  @Command(name = ["files", "upload-version"], description = "Upload files to existing version.")
  fun filesUploadVersion(
      @Option(longName = "version-id") versionId: Long,
      @Option(longName = "files") files: String,
  ): String {
    val version = versionService.getModVersion(VersionId(versionId))
    val multipartFiles = multipartFiles(files)
    fileService.uploadVersionFiles(version, multipartFiles)
    return "Uploaded ${multipartFiles.size} file(s) to version '$versionId'."
  }

  private fun parseSort(sort: String): Sort {
    val parts = sort.split(",").map { it.trim() }
    val property = parts.firstOrNull().orEmpty()
    require(property.isNotBlank()) { "Sort property is required. Example: name,asc" }
    val direction =
        if (parts.getOrNull(1).equals("desc", ignoreCase = true)) {
          Sort.Direction.DESC
        } else {
          Sort.Direction.ASC
        }
    return Sort.by(direction, property)
  }

  private fun categoryIds(raw: String?): Set<CategoryId> =
      raw
          ?.split(",")
          ?.map { it.trim() }
          ?.filter { it.isNotBlank() }
          ?.map { CategoryId(it) }
          ?.toSet()
          ?: emptySet()

  private fun multipartFiles(raw: String): List<MultipartFile> =
      raw.split(",").map { it.trim() }.filter { it.isNotBlank() }.map { multipartFile(Path.of(it)) }

  private fun multipartFile(path: Path): MultipartFile {
    require(Files.exists(path) && Files.isRegularFile(path)) { "File does not exist: $path" }
    return PathMultipartFile(path)
  }
}

private class PathMultipartFile(
    private val path: Path,
) : MultipartFile {
  private val rawBytes: ByteArray by lazy { Files.readAllBytes(path) }
  private val resolvedContentType: String by lazy {
    Files.probeContentType(path) ?: "application/octet-stream"
  }

  override fun getName(): String = path.name

  override fun getOriginalFilename(): String = path.name

  override fun getContentType(): String = resolvedContentType

  override fun isEmpty(): Boolean = getSize() == 0L

  override fun getSize(): Long = rawBytes.size.toLong()

  override fun getBytes(): ByteArray = rawBytes

  override fun getInputStream(): InputStream = Files.newInputStream(path)

  override fun transferTo(dest: File) {
    Files.copy(path, dest.toPath())
  }
}
