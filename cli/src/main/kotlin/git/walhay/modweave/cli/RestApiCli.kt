package git.walhay.modweave.cli

class RestApiCli(
    private val handler: ResourceCommandHandler = ResourceCommandHandler(),
) {
  private val shell = InteractiveShell(handler)

  fun run(args: List<String>) {
    if (args.isEmpty()) {
      shell.run()
      return
    }

    if (args.first() in setOf("help", "--help", "-h")) {
      println(usage())
      return
    }

    if (args.first() != "exec") {
      val interactiveArgs = if (args.first() == "interactive") args.drop(1) else args
      val (global, _) = parseGlobalOptions(interactiveArgs)
      shell.run(global)
      return
    }

    val (global, rest) = parseGlobalOptions(args.drop(1))
    require(rest.size >= 2) { "Missing command. Use `help`." }

    val response =
        handler.execute(
            global = global,
            resource = rest[0],
            action = rest[1],
            options = parseOptions(rest.drop(2)),
        )
    println(response)
  }
}
