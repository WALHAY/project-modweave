package git.walhay.modweave.cli

import java.nio.file.Files
import java.nio.file.Path

class InteractiveShell(
  private val handler: ResourceCommandHandler,
) {
  fun run(
      initialGlobal: GlobalOptions = GlobalOptions(baseUrl = "http://localhost:8080", username = null, password = null),
  ) {
    var global = initialGlobal
    println("Modweave interactive CLI")
    println("Type `help` for commands, `exit` to quit.")

    while (true) {
      val authState = if (global.username != null) "auth:${global.username}" else "auth:none"
      print("modweave[$authState]> ")
      val line = readLine()?.trim() ?: return
      if (line.isBlank()) continue

      try {
        when {
          line in setOf("exit", "quit") -> return
          line == "help" -> println(interactiveUsage())
          line == "config" -> {
            println("base-url=${global.baseUrl}")
            println("username=${global.username ?: "<none>"}")
          }
          line == "logout" -> {
            global = global.copy(username = null, password = null)
            println("Logged out.")
          }
          line.startsWith("set base-url ") -> {
            val value = line.removePrefix("set base-url ").trim()
            require(value.isNotBlank()) { "Base URL cannot be blank." }
            global = global.copy(baseUrl = value)
            println("Updated base URL: ${global.baseUrl}")
          }
          line.startsWith("login ") -> {
            val payload = line.removePrefix("login ").trim()
            val parts = payload.split(" ", limit = 2)
            require(parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
              "Usage: login <username> <password>"
            }
            global = global.copy(username = parts[0], password = parts[1])
            println("Logged in as ${parts[0]}.")
          }
          line.startsWith("load ") -> {
            val value = line.removePrefix("load ").trim()
            require(value.isNotBlank()) { "Usage: load <file-path>" }

            val path = Path.of(value)
            require(Files.exists(path) && Files.isRegularFile(path)) { "File does not exist: $path" }

            val commands =
                Files.readAllLines(path)
                    .map { it.trim() }
                    .filter { it.isNotBlank() && !it.startsWith("#") }

            for (command in commands) {
              when {
                command in setOf("exit", "quit") -> return
                command == "help" -> println(interactiveUsage())
                command == "config" -> {
                  println("base-url=${global.baseUrl}")
                  println("username=${global.username ?: "<none>"}")
                }
                command == "logout" -> {
                  global = global.copy(username = null, password = null)
                  println("Logged out.")
                }
                command.startsWith("set base-url ") -> {
                  val commandValue = command.removePrefix("set base-url ").trim()
                  require(commandValue.isNotBlank()) { "Base URL cannot be blank." }
                  global = global.copy(baseUrl = commandValue)
                  println("Updated base URL: ${global.baseUrl}")
                }
                command.startsWith("login ") -> {
                  val payload = command.removePrefix("login ").trim()
                  val parts = payload.split(" ", limit = 2)
                  require(parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                    "Usage: login <username> <password>"
                  }
                  global = global.copy(username = parts[0], password = parts[1])
                  println("Logged in as ${parts[0]}.")
                }
                else -> {
                  val tokens = parseInputLine(command)
                  require(tokens.size >= 2) {
                    "Command must have at least <resource> <action>. Type `help`."
                  }

                  val response =
                      handler.execute(
                          global = global,
                          resource = tokens[0],
                          action = tokens[1],
                          options = parseOptions(tokens.drop(2)),
                      )
                  println(response)
                }
              }
            }

            println("Loaded ${commands.size} command(s) from $path")
          }
          else -> {
            val tokens = parseInputLine(line)
            require(tokens.size >= 2) {
              "Command must have at least <resource> <action>. Type `help`."
            }

            val response =
                handler.execute(
                    global = global,
                    resource = tokens[0],
                    action = tokens[1],
                    options = parseOptions(tokens.drop(2)),
                )
            println(response)
          }
        }
      } catch (e: Exception) {
        println("Error: ${e.message}")
      }
    }
  }
}
