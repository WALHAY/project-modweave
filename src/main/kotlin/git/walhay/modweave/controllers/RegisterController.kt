package git.walhay.modweave.controllers

import git.walhay.modweave.user.User
import git.walhay.modweave.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

@Controller
class RegisterController
@Autowired
constructor(
    private val userService: UserService,
) {
  @GetMapping("/register")
  fun registerForm(model: Model): String {
    model.addAttribute("user", User())
    return "register-form"
  }

  @PostMapping("/register")
  fun registerSubmit(@ModelAttribute("user") user: User, model: Model): String {
    userService.registerNewUser(user)

    return "/"
  }
}
