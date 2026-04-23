package git.walhay.modweave.api.user.http

import git.walhay.modweave.api.mod.IModService
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.user.IUserService
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.user.http.dto.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: IUserService,
    private val modService: IModService,
) {
  @GetMapping
  fun getUsers(
      @RequestParam @Min(0) page: Int,
      @RequestParam @Min(1) size: Int,
      @RequestParam(required = false) username: String?,
      @SortDefault(sort = ["username"]) sort: Sort,
  ): Page<UserResponseDto> =
      userService.findUsersWithFilter(page, size, username, sort).map {
        UserResponseDto.fromUser(it)
      }

  @GetMapping("/{id}")
  fun getUser(
      @PathVariable id: String,
  ): UserResponseDto =
      userService.findUserByUsername(UserId(id)).let { UserResponseDto.fromUser(it) }

  @GetMapping("/{id}/mods")
  fun getUserMods(
      @PathVariable id: String,
      @RequestParam @Min(0) page: Int,
      @RequestParam @Min(1) size: Int,
      @SortDefault(sort = ["name"]) sort: Sort,
  ): Page<Mod> = modService.findModsOfUser(UserId(id), page, size, sort)

  @PostMapping
  fun createUser(
      @ModelAttribute @Valid dto: UserCreateDto,
  ): UserResponseDto =
      userService.createUser(dto.toUserCreateCommand()).let { UserResponseDto.fromUser(it) }

  @PatchMapping
  fun updateUser(
      @ModelAttribute @Valid dto: UserUpdateDto,
      @AuthenticationPrincipal user: UserDetails,
  ): UserResponseDto =
      userService.updateUser(UserId(user.username), dto.toUserUpdateCommand()).let {
        UserResponseDto.fromUser(it)
      }
}
