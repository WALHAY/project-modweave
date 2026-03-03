package git.walhay.modweave.api.user

import git.walhay.modweave.api.user.dto.UserDto
import git.walhay.modweave.api.user.dto.UserRegisterDto
import git.walhay.modweave.api.user.dto.UserUpdateDto
import git.walhay.modweave.api.user.exception.UserEmailExistsException
import git.walhay.modweave.api.user.exception.UserLoginExistsException
import git.walhay.modweave.api.user.exception.UserNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
	private val userRepository: UserRepository,
	private val passwordEncoder: PasswordEncoder
) {

	fun findUserById(login: String): User =
		userRepository.findByLoginIgnoreCase(login) ?: throw UserNotFoundException("User with login=$login not found")

	fun registerNewUser(register: UserRegisterDto): User {
		if (userRepository.existsByLoginIgnoreCase(register.login)) {
			throw UserLoginExistsException("Login ${register.login} already in use")
		}

		if (userRepository.existsByEmailIgnoreCase(register.email)) {
			throw UserEmailExistsException("Email ${register.email} already in use")
		}

		val encodedPass =
			passwordEncoder.encode(register.password)
				?: throw IllegalStateException("Failed to encode password")

		val user = User(register.login, register.username, register.email, encodedPass)

		return userRepository.save(user)
	}

	fun updateUserProfile(login: String, update: UserUpdateDto): User {
		val user: User =
			userRepository.findByLoginIgnoreCase(login)
				?: throw UserNotFoundException("User with login=$login not found")

		update.username?.let { user.username = it }
		update.password?.let {
			user.password =
				passwordEncoder.encode(it) ?: throw IllegalStateException("Failed to encode password")
		}
		update.email?.let {
			if (it != user.email && userRepository.existsByEmailIgnoreCase(it)) {
				throw UserEmailExistsException("Email $it already in use")
			}
			user.email = it
		}

		return userRepository.save(user)
	}
}
