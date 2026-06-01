package git.walhay.modweave.api.user

import git.walhay.modweave.api.common.paging.PageSizePolicy
import git.walhay.modweave.api.user.command.UserCreateCommand
import git.walhay.modweave.api.user.command.UserUpdateCommand
import git.walhay.modweave.api.user.exception.UserEmailExistsException
import git.walhay.modweave.api.user.exception.UserLoginExistsException
import git.walhay.modweave.api.user.exception.UserNotFoundException
import git.walhay.modweave.api.user.repository.UserRepository
import mu.KLogger
import mu.KotlinLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val pageSizePolicy: PageSizePolicy,
) : IUserService {
  private val logger: KLogger = KotlinLogging.logger {}

  @Cacheable("users", key = "#userId")
  override fun findUserByUsername(userId: UserId): User {
    logger.debug { "Fetching user by username: $userId" }
    return userRepository.findByUsername(userId) ?: throw UserNotFoundException(userId)
  }

  override fun findUsersWithFilter(
      page: Int,
      size: Int,
      name: String?,
      sort: Sort,
  ): Page<User> {
    logger.debug {
      "Fetching users with filter - page: $page, size: $size, name: $name, sort: $sort"
    }
    val pageRequest = PageRequest.of(page, pageSizePolicy.normalize(size), sort)
    if (name == null) {
      return userRepository.findAll(pageRequest)
    }
    return userRepository.findAll(name, pageRequest)
  }

  @CachePut("users", key = "#result.username")
  override fun createUser(command: UserCreateCommand): User {
    logger.info { "Creating new user: ${command.username}" }
    if (userRepository.existsByUsername(command.username)) {
      logger.warn { "User creation failed - login already exists: ${command.username}" }
      throw UserLoginExistsException(command.username)
    }

    if (userRepository.existsByEmail(command.email)) {
      logger.warn { "User creation failed - email already exists: ${command.email}" }
      throw UserEmailExistsException(command.email)
    }

    val encodedPass =
        passwordEncoder.encode(command.password)
            ?: throw IllegalStateException("Failed to encode password")

    val user =
        command
            .let { (username, name, _, email) -> User(username, name, email, encodedPass) }
            .let { userRepository.save(it) }

    logger.info { "User created successfully: ${user.username}" }
    return user
  }

  @CacheEvict("users", key = "#userId")
  @PreAuthorize("@accessSecurity.isSelfOrAdmin(#userId)")
  override fun updateUser(
      userId: UserId,
      command: UserUpdateCommand,
  ): User {
    logger.info { "Updating user: $userId" }
    val user: User = findUserByUsername(userId)

    command.name?.let {
      logger.debug { "Updating username for user $userId to $it" }
      user.name = it
    }
    command.password?.let {
      logger.debug { "Updating password for user $userId" }
      user.password =
          passwordEncoder.encode(it) ?: throw IllegalStateException("Failed to encode password")
    }
    command.email
        ?.takeUnless { it == user.email }
        ?.let {
          if (userRepository.existsByEmail(it)) {
            logger.warn { "User update failed - email already in use: $it" }
            throw UserEmailExistsException("Email $it already in use")
          }
          logger.debug { "Updating email for user $userId to $it" }
          user.email = it
        }

    val updatedUser = userRepository.save(user)
    logger.info { "User updated successfully: $userId" }
    return updatedUser
  }
}
