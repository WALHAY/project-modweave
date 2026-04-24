package git.walhay.modweave.cli

import java.nio.file.Path

fun parseGlobalOptions(args: List<String>): Pair<GlobalOptions, List<String>> {
  var i = 0
  var baseUrl = "http://localhost:8080"
  var username: String? = null
  var password: String? = null

  while (i < args.size && args[i].startsWith("--")) {
    val key = args[i].removePrefix("--")
    val value = args.getOrNull(i + 1) ?: error("Missing value for --$key")
    when (key) {
      "base-url" -> baseUrl = value
      "username" -> username = value
      "password" -> password = value
      else -> break
    }
    i += 2
  }

  return GlobalOptions(baseUrl = baseUrl, username = username, password = password) to args.drop(i)
}

fun parseOptions(args: List<String>): Map<String, List<String>> {
  if (args.isEmpty()) return emptyMap()
  require(args.size % 2 == 0) { "Options must be key-value pairs, like --name value" }

  val options = mutableMapOf<String, MutableList<String>>()
  var i = 0
  while (i < args.size) {
    val key = args[i]
    require(key.startsWith("--")) { "Invalid option: $key" }
    val name = key.removePrefix("--")
    val value = args[i + 1]
    options.computeIfAbsent(name) { mutableListOf() }.add(value)
    i += 2
  }
  return options
}

fun parseInputLine(line: String): List<String> {
  val result = mutableListOf<String>()
  val current = StringBuilder()
  var inQuotes = false
  var quoteChar = '"'

  for (ch in line) {
    if ((ch == '"' || ch == '\'') && (!inQuotes || ch == quoteChar)) {
      if (inQuotes && ch == quoteChar) {
        inQuotes = false
      } else if (!inQuotes) {
        inQuotes = true
        quoteChar = ch
      }
      continue
    }

    if (ch.isWhitespace() && !inQuotes) {
      if (current.isNotEmpty()) {
        result.add(current.toString())
        current.clear()
      }
      continue
    }

    current.append(ch)
  }

  require(!inQuotes) { "Unclosed quote in input." }
  if (current.isNotEmpty()) result.add(current.toString())
  return result
}

fun required(options: Map<String, List<String>>, key: String): String =
    options[key]?.firstOrNull()?.takeIf { it.isNotBlank() } ?: error("Missing --$key")

fun optional(options: Map<String, List<String>>, key: String): String? =
    options[key]?.firstOrNull()?.takeIf { it.isNotBlank() }

fun requiredPath(options: Map<String, List<String>>, key: String): Path = Path.of(required(options, key))

fun requiredPaths(options: Map<String, List<String>>, key: String): List<Path> =
    required(options, key)
        .split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .map { Path.of(it) }
        .also { require(it.isNotEmpty()) { "--$key must contain at least one path" } }

fun Map<String, List<String>>.singleOrDefault(key: String, defaultValue: String): String =
    this[key]?.firstOrNull()?.takeIf { it.isNotBlank() } ?: defaultValue
