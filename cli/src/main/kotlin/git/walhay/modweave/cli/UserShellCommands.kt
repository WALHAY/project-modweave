package git.walhay.modweave.cli

import git.walhay.modweave.api.user.IUserService
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.user.command.UserCreateCommand
import git.walhay.modweave.api.user.command.UserUpdateCommand
import org.springframework.shell.core.command.annotation.Command
import org.springframework.shell.core.command.annotation.Option
import org.springframework.stereotype.Component

@Component
class UserShellCommands(
    private val userService: IUserService,
) : ShellCommandSupport() {
  @Command(name = ["users", "list"], description = "List users with paging.")
  fun usersList(
      @Option(longName = "page", defaultValue = "0") page: Int,
      @Option(longName = "size", defaultValue = "20") size: Int,
      @Option(longName = "name", required = false) name: String?,
      @Option(longName = "sort", defaultValue = "username,asc") sort: String,
  ): Any = renderPage(userService.findUsersWithFilter(page, size, name, parseSort(sort)))

  @Command(name = ["users", "get"], description = "Get user by username.")
  fun usersGet(
      @Option(longName = "id") id: String,
  ): Any = renderValue(userService.findUserByUsername(UserId(id)))

  @Command(name = ["users", "create"], description = "Create user.")
  fun usersCreate(
      @Option(longName = "username") username: String,
      @Option(longName = "name") name: String,
      @Option(longName = "password") password: String,
      @Option(longName = "email") email: String,
  ): Any =
      renderValue(
          userService.createUser(
              UserCreateCommand(
                  username = UserId(username),
                  name = name,
                  password = password,
                  email = email,
              ),
          ),
      )

  @Command(name = ["users", "update"], description = "Update user.")
  fun usersUpdate(
      @Option(longName = "id") id: String,
      @Option(longName = "name", required = false) name: String?,
      @Option(longName = "password", required = false) password: String?,
      @Option(longName = "email", required = false) email: String?,
  ): Any = renderValue(userService.updateUser(UserId(id), UserUpdateCommand(name, password, email)))
}
