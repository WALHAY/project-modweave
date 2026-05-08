package git.walhay.modweave.cli

import git.walhay.modweave.api.user.IUserService
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.user.command.UserCreateCommand
import git.walhay.modweave.api.user.command.UserUpdateCommand
import git.walhay.modweave.api.user.http.dto.UserResponseDto
import git.walhay.modweave.api.user.http.dto.fromUser
import org.springframework.shell.core.command.annotation.Command
import org.springframework.shell.core.command.annotation.Option
import org.springframework.stereotype.Component

@Component
class UserShellCommands(
    private val userService: IUserService,
) : ShellCommandSupport() {
  @Command(name = ["user", "list"], description = "List user with paging.")
  fun userList(
      @Option(longName = "page", defaultValue = "0") page: Int,
      @Option(longName = "size", defaultValue = "20") size: Int,
      @Option(longName = "name", required = false) name: String?,
      @Option(longName = "sort", defaultValue = "username,asc") sort: String,
  ): Any =
      renderPage(userService.findUsersWithFilter(page, size, name, parseSort(sort)).map {
        UserResponseDto.fromUser(it)
      })

  @Command(name = ["user", "get"], description = "Get user by username.")
  fun userGet(
      @Option(longName = "id") id: String,
  ): Any = renderValue(UserResponseDto.fromUser(userService.findUserByUsername(UserId(id))))

  @Command(name = ["user", "create"], description = "Create user.")
  fun userCreate(
      @Option(longName = "username") username: String,
      @Option(longName = "name") name: String,
      @Option(longName = "password") password: String,
      @Option(longName = "email") email: String,
  ): Any =
      renderValue(
          UserResponseDto.fromUser(
              userService.createUser(
                  UserCreateCommand(
                      username = UserId(username),
                      name = name,
                      password = password,
                      email = email,
                  ),
              ),
          ),
      )

  @Command(name = ["user", "update"], description = "Update user.")
  fun userUpdate(
      @Option(longName = "id") id: String,
      @Option(longName = "name", required = false) name: String?,
      @Option(longName = "password", required = false) password: String?,
      @Option(longName = "email", required = false) email: String?,
  ): Any =
      renderValue(
          UserResponseDto.fromUser(userService.updateUser(UserId(id), UserUpdateCommand(name, password, email))),
      )
}
