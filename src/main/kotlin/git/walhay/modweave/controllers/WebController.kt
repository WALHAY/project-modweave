package git.walhay.modweave.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class WebController {

  @GetMapping("/login") fun login(): String = "login.html"

  @GetMapping("/") fun index(): String = "index.html"
}
