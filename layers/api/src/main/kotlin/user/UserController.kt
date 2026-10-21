package git.walhay.modweave.api.user

import git.walhay.modweave.api.collection.CollectionResponseDto
import git.walhay.modweave.api.common.apiLink
import git.walhay.modweave.api.common.forApi
import git.walhay.modweave.api.mod.ModResponseDto
import git.walhay.modweave.domain.collection.ICollectionService
import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.mod.IModService
import git.walhay.modweave.domain.user.IUserService
import git.walhay.modweave.domain.user.UserId
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import mu.KLogger
import mu.KotlinLogging
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: IUserService,
    private val collectionService: ICollectionService,
    private val modService: IModService,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @GetMapping
  fun getUsers(
      @RequestParam @Min(0) page: Int,
      @RequestParam @Min(1) size: Int,
      @RequestParam(required = false) username: String?,
      @SortDefault(sort = ["username"]) sort: Sort,
  ): Page<UserResponseDto> {
    logger.debug { "GET /users - page: $page, size: $size, username: $username" }
    return userService
        .findUsersWithFilter(
            page, size, username, sort.forApi("username", "name", identity = "username"))
        .map { UserResponseDto.fromUser(it) }
  }

  @GetMapping("/{id}")
  fun getUser(
      @PathVariable id: String,
  ): UserResponseDto {
    logger.info { "GET /users/$id" }
    return userService.findUserByUsername(UserId(id)).let { UserResponseDto.fromUser(it) }
  }

  @GetMapping("/{id}/mods")
  fun getUserMods(
      @PathVariable id: String,
      @RequestParam @Min(0) page: Int,
      @RequestParam @Min(1) size: Int,
      @SortDefault(sort = ["name"]) sort: Sort,
  ): Page<ModResponseDto> {
    logger.info { "GET /users/$id/mods - page: $page, size: $size" }
    userService.findUserByUsername(UserId(id))
    return modService.findModsOfUser(UserId(id), page, size, sort.forApi("name", "id")).map {
      ModResponseDto.fromMod(it)
    }
  }

  @GetMapping("/{id}/collections")
  fun getUserCollections(
      @PathVariable id: String,
      @RequestParam @Min(0) page: Int,
      @RequestParam @Min(1) size: Int,
      @SortDefault(sort = ["name"]) sort: Sort,
  ): Page<CollectionResponseDto> {
    logger.info { "GET /users/$id/collections - page: $page, size: $size" }
    userService.findUserByUsername(UserId(id))
    return collectionService
        .findCollectionsOfUser(UserId(id), page, size, sort.forApi("name", "id"))
        .map { CollectionResponseDto.fromCollection(it) }
  }

  @PostMapping(
      version = "1", consumes = ["application/x-www-form-urlencoded", "multipart/form-data"])
  fun createUser(
      @ModelAttribute @Valid dto: UserCreateDto,
  ): UserResponseDto {
    logger.info { "POST /users - creating user: ${dto.username}" }
    return userService.createUser(dto.toUserCreateCommand()).let { UserResponseDto.fromUser(it) }
  }

  @PostMapping(version = "2", consumes = ["application/x-www-form-urlencoded"])
  fun registerUser(@ModelAttribute @Valid dto: UserCreateDto): ResponseEntity<UserResponseDto> {
    val result = createUser(dto)
    return ResponseEntity.created(apiLink(UserController::getUser, result.username).link.toUri())
        .body(result)
  }

  @PatchMapping(version = "1")
  fun updateUser(
      @ModelAttribute @Valid dto: UserUpdateDto,
      @AuthenticationPrincipal user: UserDetails?,
  ): UserResponseDto {
    val username = requireUsername(user)
    logger.info { "PATCH /users - updating user: $username" }
    return userService.updateUser(UserId(username), dto.toUserUpdateCommand()).let {
      UserResponseDto.fromUser(it)
    }
  }

  @PatchMapping("/me", version = "2", consumes = ["application/x-www-form-urlencoded"])
  fun updateCurrentUser(
      @ModelAttribute @Valid dto: UserUpdateDto,
      @AuthenticationPrincipal user: UserDetails?,
      @RequestParam fields: Map<String, String>,
  ): UserResponseDto {
    require(fields.keys.all { it in setOf("name", "password", "email") }) {
      "Unknown profile field"
    }
    return updateUser(dto, user)
  }

  private fun requireUsername(user: UserDetails?): String =
      user?.username ?: throw ResponseStatusException(UNAUTHORIZED, "Authentication required")
}
