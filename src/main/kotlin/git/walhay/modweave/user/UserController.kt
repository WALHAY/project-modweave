package git.walhay.modweave.user

import git.walhay.modweave.mods.Mod
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/profile")
class UserController {

    @Autowired
    private var userRepository: UserRepository? = null

    @GetMapping("/{login}")
    @ResponseBody
    fun getUser(@PathVariable login: String): User? = userRepository?.findById(login)?.get()

    @GetMapping("/{login}/mods")
    fun getUserMods(@PathVariable login: String): MutableSet<Mod>? = userRepository?.findById(login)?.get()?.mods
}