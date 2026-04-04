package git.walhay.modweave.api.user.http

import git.walhay.modweave.api.user.IUserService
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.user.http.dto.*
import git.walhay.modweave.api.user.toUserResponseDto
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.data.web.SortDefault
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(private val userService: IUserService) {

  @GetMapping
  fun getUsers(
      @RequestParam @Min(0) page: Int,
      @RequestParam @Min(1) size: Int,
      @RequestParam(required = false) username: String?,
      @SortDefault(sort = ["username"]) sort: Sort
  ): Page<UserResponseDto> =
      userService.findUsersWithFilter(page, size, username, sort).map { it.toUserResponseDto() }

  @GetMapping("/{userId}")
  fun getUser(@PathVariable userId: UserId): UserResponseDto =
      userService.findUserByUsername(userId).toUserResponseDto()

  @PostMapping
  fun registerUser(@ModelAttribute @Valid dto: UserRegisterDto): UserResponseDto =
      userService.registerNewUser(dto.toUserCreateCommand()).toUserResponseDto()

  @PatchMapping
  fun changeUserInfo(@ModelAttribute @Valid dto: UserUpdateDto): UserResponseDto {
    return SecurityContextHolder.getContext().authentication?.name?.let {
      userService.updateUserProfile(UserId(it), dto.toUserUpdateCommand()).toUserResponseDto()
    } ?: throw Exception("Authentication fail")
  }
}
