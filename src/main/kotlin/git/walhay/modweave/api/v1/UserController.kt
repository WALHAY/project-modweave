package git.walhay.modweave.api.v1

import git.walhay.modweave.dto.RegisterForm
import git.walhay.modweave.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class UserController @Autowired constructor(private val userService: UserService) {

  @PostMapping("/register")
  fun registerUser(@ModelAttribute registerForm: RegisterForm) {
    userService.registerNewUser(registerForm)
  }
}