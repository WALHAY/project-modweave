package git.walhay.modweave.cli

fun usage(): String =
    """
    Modweave REST CLI
    
    Usage:
      ./gradlew -p cli run
      ./gradlew -p cli run --args="interactive"
      ./gradlew -p cli run --args="interactive --username user --password pass"
      ./gradlew -p cli run --args="exec <resource> <action> [--key value ...]"
    
    Notes:
      - REPL is the default mode.
      - Use `exec` for one-shot command execution.
      - Global options in exec mode:
        --base-url http://localhost:8080 [--username user --password pass]
    
    Resources/actions:
      categories list|create|update|delete
      games list|get|create
      users list|get|mods|create|update
      mods list|get|versions|create|delete
      versions create
      collections get|mods|create|add-mod|delete|delete-mod
      comments create|delete
      files download
    
    Examples:
      ./gradlew -p cli run
      ./gradlew -p cli run --args="exec categories list"
      ./gradlew -p cli run --args="exec --username admin --password secret mods create --id frostfall --name Frostfall --game-id skyrim --version-name 1.0.0 --image /tmp/logo.png --files /tmp/mod.zip,/tmp/readme.txt"
    """.trimIndent()

fun interactiveUsage(): String =
    """
    Interactive commands:
      help
      exit | quit
      config
      set base-url <url>
      login <username> <password>
      logout
      load <file-path>
        - Executes one command per line from file
        - Ignores blank lines and lines starting with '#'
    
    API command format:
      <resource> <action> [--key value ...]
    
    Example:
      mods create --id frostfall --name "Frost Fall" --game-id skyrim --version-name 1.0.0 --image /tmp/logo.png --files /tmp/mod.zip,/tmp/readme.txt
    """.trimIndent()
