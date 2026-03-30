package git.walhay.modweave.api.user

import git.walhay.modweave.api.user.dto.UserDto
import git.walhay.modweave.api.user.dto.UserRegisterDto
import git.walhay.modweave.api.user.dto.UserUpdateDto
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import java.util.*
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
  ): Page<UserDto> =
      userService.findUsersWithFilter(page, size, username, sort).map { it.toUserDto() }

  @GetMapping("/{login}")
  fun getUser(@PathVariable login: UUID): UserDto = userService.findUserById(login).toUserDto()

  @PostMapping
  fun registerUser(@ModelAttribute @Valid registerForm: UserRegisterDto): UserDto =
      userService.registerNewUser(registerForm).toUserDto()

  @PatchMapping
  fun changeUserInfo(@ModelAttribute @Valid userUpdateDto: UserUpdateDto): UserDto {
    return SecurityContextHolder.getContext().authentication?.name?.let {
      userService.updateUserProfile(UUID.fromString(it), userUpdateDto).toUserDto()
    } ?: throw Exception("Authentication fail")
  }
}
