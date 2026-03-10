package git.walhay.modweave.api.user

import git.walhay.modweave.api.user.dto.UserDto
import git.walhay.modweave.api.user.dto.UserRegisterDto
import git.walhay.modweave.api.user.dto.UserUpdateDto
import jakarta.validation.Valid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

	@GetMapping("/{login}")
	fun getUser(@PathVariable login: String): UserDto = userService.findUserById(login).toUserDto()

	@PostMapping
	fun registerUser(@ModelAttribute @Valid registerForm: UserRegisterDto): UserDto =
		userService.registerNewUser(registerForm).toUserDto()

	@PatchMapping
	fun changeUserInfo(@ModelAttribute @Valid userUpdateDTO: UserUpdateDto): UserDto {
		return SecurityContextHolder.getContext().authentication?.name?.let {
			userService.updateUserProfile(it, userUpdateDTO).toUserDto()
		} ?: throw Exception("Authentication fail")
	}
}
