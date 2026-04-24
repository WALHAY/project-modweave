package git.walhay.modweave.cli

class ResourceCommandHandler(
    private val apiClient: ApiHttpClient = ApiHttpClient(),
) {
  fun execute(
      global: GlobalOptions,
      resource: String,
      action: String,
      options: Map<String, List<String>>,
  ): String =
      when (resource) {
        "categories" -> handleCategories(global, action, options)
        "games" -> handleGames(global, action, options)
        "users" -> handleUsers(global, action, options)
        "mods" -> handleMods(global, action, options)
        "versions" -> handleVersions(global, action, options)
        "collections" -> handleCollections(global, action, options)
        "comments" -> handleComments(global, action, options)
        "files" -> handleFiles(global, action, options)
        else -> error("Unknown resource: $resource")
      }

  private fun handleCategories(
      global: GlobalOptions,
      action: String,
      options: Map<String, List<String>>,
  ): String =
      when (action) {
        "list" -> apiClient.get(global, "/categories")
        "create" ->
            apiClient.formRequest(
                global = global,
                method = "POST",
                path = "/categories",
                form = mapOf("name" to required(options, "name"), "description" to optional(options, "description")),
            )
        "update" ->
            apiClient.formRequest(
                global = global,
                method = "PATCH",
                path = "/categories",
                form = mapOf("name" to required(options, "name"), "description" to optional(options, "description")),
            )
        "delete" ->
            apiClient.delete(
                global = global,
                path = "/categories",
                query = mapOf("categoryId" to required(options, "category-id")),
            )
        else -> error("Unknown categories action: $action")
      }

  private fun handleGames(
      global: GlobalOptions,
      action: String,
      options: Map<String, List<String>>,
  ): String =
      when (action) {
        "list" ->
            apiClient.get(
                global = global,
                path = "/games",
                query =
                    mapOf(
                        "page" to options.singleOrDefault("page", "0"),
                        "size" to options.singleOrDefault("size", "20"),
                        "name" to optional(options, "name"),
                        "sort" to options.singleOrDefault("sort", "name,asc"),
                    ),
            )
        "get" -> apiClient.get(global, "/games/${required(options, "game-id")}")
        "create" ->
            apiClient.multipartRequest(
                global = global,
                method = "POST",
                path = "/games",
                fields =
                    mapOf(
                        "id" to required(options, "id"),
                        "name" to required(options, "name"),
                        "description" to optional(options, "description"),
                    ),
                files = mapOf("image" to listOf(requiredPath(options, "image"))),
            )
        else -> error("Unknown games action: $action")
      }

  private fun handleUsers(
      global: GlobalOptions,
      action: String,
      options: Map<String, List<String>>,
  ): String =
      when (action) {
        "list" ->
            apiClient.get(
                global = global,
                path = "/users",
                query =
                    mapOf(
                        "page" to options.singleOrDefault("page", "0"),
                        "size" to options.singleOrDefault("size", "20"),
                        "username" to optional(options, "username"),
                        "sort" to options.singleOrDefault("sort", "username,asc"),
                    ),
            )
        "get" -> apiClient.get(global, "/users/${required(options, "id")}")
        "mods" ->
            apiClient.get(
                global = global,
                path = "/users/${required(options, "id")}/mods",
                query =
                    mapOf(
                        "page" to options.singleOrDefault("page", "0"),
                        "size" to options.singleOrDefault("size", "20"),
                        "sort" to options.singleOrDefault("sort", "name,asc"),
                    ),
            )
        "create" ->
            apiClient.formRequest(
                global = global,
                method = "POST",
                path = "/users",
                form =
                    mapOf(
                        "username" to required(options, "username"),
                        "name" to required(options, "name"),
                        "password" to required(options, "password"),
                        "email" to required(options, "email"),
                    ),
            )
        "update" ->
            apiClient.formRequest(
                global = global,
                method = "PATCH",
                path = "/users",
                form =
                    mapOf(
                        "username" to optional(options, "username"),
                        "password" to optional(options, "password"),
                        "email" to optional(options, "email"),
                    ),
            )
        else -> error("Unknown users action: $action")
      }

  private fun handleMods(
      global: GlobalOptions,
      action: String,
      options: Map<String, List<String>>,
  ): String =
      when (action) {
        "list" ->
            apiClient.get(
                global = global,
                path = "/mods",
                query =
                    mapOf(
                        "page" to options.singleOrDefault("page", "0"),
                        "size" to options.singleOrDefault("size", "20"),
                        "name" to optional(options, "name"),
                        "sort" to options.singleOrDefault("sort", "name,asc"),
                    ),
            )
        "get" -> apiClient.get(global, "/mods/${required(options, "mod-id")}")
        "versions" ->
            apiClient.get(
                global = global,
                path = "/mods/${required(options, "mod-id")}/versions",
                query =
                    mapOf(
                        "page" to options.singleOrDefault("page", "0"),
                        "size" to options.singleOrDefault("size", "20"),
                        "sort" to options.singleOrDefault("sort", "id,desc"),
                    ),
            )
        "create" ->
            apiClient.multipartRequest(
                global = global,
                method = "POST",
                path = "/mods",
                fields =
                    mapOf(
                        "id" to required(options, "id"),
                        "name" to required(options, "name"),
                        "description" to optional(options, "description"),
                        "versionName" to required(options, "version-name"),
                        "gameId" to required(options, "game-id"),
                        "categories" to optional(options, "categories"),
                    ),
                files =
                    mapOf(
                        "image" to listOf(requiredPath(options, "image")),
                        "files" to requiredPaths(options, "files"),
                    ),
            )
        "delete" -> apiClient.delete(global, "/mods/${required(options, "mod-id")}")
        else -> error("Unknown mods action: $action")
      }

  private fun handleVersions(
      global: GlobalOptions,
      action: String,
      options: Map<String, List<String>>,
  ): String =
      when (action) {
        "create" ->
            apiClient.multipartRequest(
                global = global,
                method = "POST",
                path = "/mods/${required(options, "mod-id")}/versions",
                fields =
                    mapOf(
                        "name" to required(options, "name"),
                        "changes" to optional(options, "changes"),
                    ),
                files = mapOf("files" to requiredPaths(options, "files")),
            )
        else -> error("Unknown versions action: $action")
      }

  private fun handleCollections(
      global: GlobalOptions,
      action: String,
      options: Map<String, List<String>>,
  ): String =
      when (action) {
        "get" -> apiClient.get(global, "/collections/${required(options, "collection-id")}")
        "mods" ->
            apiClient.get(
                global = global,
                path = "/collections/${required(options, "collection-id")}/mods",
                query =
                    mapOf(
                        "page" to options.singleOrDefault("page", "0"),
                        "size" to options.singleOrDefault("size", "20"),
                        "sort" to options.singleOrDefault("sort", "index,asc"),
                    ),
            )
        "create" ->
            apiClient.formRequest(
                global = global,
                method = "POST",
                path = "/collections",
                form =
                    mapOf(
                        "name" to required(options, "name"),
                        "description" to optional(options, "description"),
                    ),
            )
        "add-mod" ->
            apiClient.put(
                global = global,
                path = "/collections/${required(options, "collection-id")}",
                query =
                    mapOf(
                        "modId" to required(options, "mod-id"),
                        "index" to optional(options, "index"),
                    ),
            )
        "delete" -> apiClient.delete(global, "/collections/${required(options, "collection-id")}")
        "delete-mod" ->
            apiClient.delete(
                global = global,
                path = "/collections/${required(options, "collection-id")}/mods/${required(options, "mod-id")}",
            )
        else -> error("Unknown collections action: $action")
      }

  private fun handleComments(
      global: GlobalOptions,
      action: String,
      options: Map<String, List<String>>,
  ): String =
      when (action) {
        "create" ->
            apiClient.formRequest(
                global = global,
                method = "POST",
                path = "/comments",
                form =
                    mapOf(
                        "content" to required(options, "content"),
                        "modId" to required(options, "mod-id"),
                    ),
            )
        "delete" -> apiClient.delete(global, "/comments/${required(options, "id")}")
        else -> error("Unknown comments action: $action")
      }

  private fun handleFiles(
      global: GlobalOptions,
      action: String,
      options: Map<String, List<String>>,
  ): String =
      when (action) {
        "download" ->
            apiClient.get(
                global = global,
                path = "/files/${required(options, "bucket")}/${required(options, "file-id")}/download",
            )
        else -> error("Unknown files action: $action")
      }
}
