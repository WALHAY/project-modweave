package git.walhay.modweave.api.user

import git.walhay.modweave.api.user.dto.UserRegisterDto
import git.walhay.modweave.api.user.dto.UserResponseDto
import git.walhay.modweave.api.user.dto.UserUpdateDto
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

  @GetMapping("/{login}")
  fun getUser(@PathVariable username: UserId): UserResponseDto =
      userService.findUserByUsername(username).toUserResponseDto()

  @PostMapping
  fun registerUser(@ModelAttribute @Valid registerForm: UserRegisterDto): UserResponseDto =
      userService.registerNewUser(registerForm).toUserResponseDto()

  @PatchMapping
  fun changeUserInfo(@ModelAttribute @Valid userUpdateDto: UserUpdateDto): UserResponseDto {
    return SecurityContextHolder.getContext().authentication?.name?.let {
      userService.updateUserProfile(UserId(it), userUpdateDto).toUserResponseDto()
    } ?: throw Exception("Authentication fail")
  }
}
