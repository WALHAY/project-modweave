package git.walhay.modweave.api.user

import git.walhay.modweave.api.user.dto.UserDto
import git.walhay.modweave.api.user.dto.UserRegisterDto
import git.walhay.modweave.api.user.dto.UserUpdateDto
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController @Autowired constructor(private val userService: UserService) {

  @GetMapping("/{login}") fun getUser(@PathVariable login: String) = userService.findUserById(login)

  @PostMapping
  fun registerUser(@ModelAttribute @Valid registerForm: UserRegisterDto) =
      userService.registerNewUser(registerForm)

  @PatchMapping
  fun changeUserInfo(@ModelAttribute @Valid userUpdateDTO: UserUpdateDto): UserDto {
    return SecurityContextHolder.getContext().authentication?.name?.let {
      userService.updateUserProfile(it, userUpdateDTO)
    } ?: throw Exception("Authentication fail")
  }
}
