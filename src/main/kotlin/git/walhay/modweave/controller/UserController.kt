package git.walhay.modweave.controller

import git.walhay.modweave.dto.UserRegisterDTO
import git.walhay.modweave.dto.UserUpdateDTO
import git.walhay.modweave.model.User
import git.walhay.modweave.repository.UserRepository
import git.walhay.modweave.service.UserService
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController
@Autowired
constructor(private val userService: UserService, private val userRepository: UserRepository) {

  @GetMapping
  fun getUsers(@RequestParam page: Int, @RequestParam pageSize: Int) =
      userRepository.findAll(PageRequest.of(page, pageSize))

  @GetMapping("/{login}")
  fun getUser(@PathVariable login: String) = userRepository.findById(login.lowercase())

  @PostMapping
  fun registerUser(@ModelAttribute @Valid registerForm: UserRegisterDTO) =
      userService.registerNewUser(registerForm)

  @PatchMapping
  fun changeUserInfo(@ModelAttribute @Valid userUpdateDTO: UserUpdateDTO): User {
    return SecurityContextHolder.getContext().authentication?.name?.let {
      userService.updateUserProfile(it, userUpdateDTO)
    } ?: throw Exception("Authentication fail")
  }
}
