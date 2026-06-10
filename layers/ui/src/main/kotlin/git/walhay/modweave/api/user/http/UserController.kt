package git.walhay.modweave.api.user.http

import git.walhay.modweave.api.collection.ICollectionService
import git.walhay.modweave.api.collection.http.dto.CollectionResponseDto
import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.http.dto.ModResponseDto
import git.walhay.modweave.api.user.IUserService
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.user.http.dto.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import mu.KLogger
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.http.HttpStatus.UNAUTHORIZED
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
    return userService.findUsersWithFilter(page, size, username, sort).map {
      UserResponseDto.fromUser(it)
    }
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
    return modService.findModsOfUser(UserId(id), page, size, sort).map {
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
    return collectionService.findCollectionsOfUser(UserId(id), page, size, sort).map {
      CollectionResponseDto.fromCollection(it)
    }
  }

  @PostMapping
  fun createUser(
      @ModelAttribute @Valid dto: UserCreateDto,
  ): UserResponseDto {
    logger.info { "POST /users - creating user: ${dto.username}" }
    return userService.createUser(dto.toUserCreateCommand()).let { UserResponseDto.fromUser(it) }
  }

  @PatchMapping
  fun updateUser(
      @ModelAttribute @Valid dto: UserUpdateDto,
      @AuthenticationPrincipal user: UserDetails?,
  ): UserResponseDto {
    val username =
        user?.username ?: throw ResponseStatusException(UNAUTHORIZED, "Authentication required")
    logger.info { "PATCH /users - updating user: $username" }
    return userService.updateUser(UserId(username), dto.toUserUpdateCommand()).let {
      UserResponseDto.fromUser(it)
    }
  }
}
