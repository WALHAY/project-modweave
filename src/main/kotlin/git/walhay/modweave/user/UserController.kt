package git.walhay.modweave.user

import git.walhay.modweave.controllers.RegisterForm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController @Autowired constructor(private val userService: UserService) {

  @PostMapping("/register")
  fun registerUser(@ModelAttribute registerForm: RegisterForm) {
    userService.registerNewUser(registerForm)
  }
}
