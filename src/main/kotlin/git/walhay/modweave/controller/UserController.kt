package git.walhay.modweave.controller

import git.walhay.modweave.dto.UserDto
import git.walhay.modweave.dto.UserRegisterDTO
import git.walhay.modweave.dto.UserUpdateDTO
import git.walhay.modweave.service.UserService
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController @Autowired constructor(private val userService: UserService) {

  @GetMapping("/{login}") fun getUser(@PathVariable login: String) = userService.findUserById(login)

  @PostMapping
  fun registerUser(@ModelAttribute @Valid registerForm: UserRegisterDTO) =
      userService.registerNewUser(registerForm)

  @PatchMapping
  fun changeUserInfo(@ModelAttribute @Valid userUpdateDTO: UserUpdateDTO): UserDto {
    return SecurityContextHolder.getContext().authentication?.name?.let {
      userService.updateUserProfile(it, userUpdateDTO)
    } ?: throw Exception("Authentication fail")
  }
}
