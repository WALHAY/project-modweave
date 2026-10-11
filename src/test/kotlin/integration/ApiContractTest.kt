package git.walhay.modweave.integration

import com.atlassian.oai.validator.model.Request
import com.atlassian.oai.validator.model.SimpleRequest
import com.atlassian.oai.validator.model.SimpleResponse
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import git.walhay.modweave.testutils.OpenApiContract
import git.walhay.modweave.testutils.StorageTestInfrastructure
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

/** Real HTTP -> security -> controller -> service -> repository -> PostgreSQL/S3, without mocks. */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties =
        [
            "spring.cache.type=none",
            "storage.cleanup.enabled=false",
            "spring.servlet.multipart.max-file-size=1KB",
            "spring.servlet.multipart.max-request-size=4KB",
            "security.jwt.secret=ZmFrZS1zZWNyZXQtc2VjdXJlLWtleS1mb3ItanVzdC1kZXZlbG9wbWVudA==",
        ])
class ApiContractTest {
  companion object {
    private val contract = OpenApiContract()

    @JvmStatic
    @DynamicPropertySource
    fun properties(registry: DynamicPropertyRegistry) {
      registry.add("spring.datasource.url") { StorageTestInfrastructure.postgres.jdbcUrl }
      registry.add("spring.datasource.username") { StorageTestInfrastructure.postgres.username }
      registry.add("spring.datasource.password") { StorageTestInfrastructure.postgres.password }
      registry.add("storage.s3.endpoint") { StorageTestInfrastructure.endpoint }
    }
  }

  @LocalServerPort private var port: Int = 0
  @Autowired private lateinit var jdbc: JdbcTemplate
  private val http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()
  private val mapper = ObjectMapper()
  private val visited = mutableSetOf<String>()

  @BeforeEach
  fun resetFixtures() {
    jdbc.execute(
        "truncate modweave.users, modweave.games, modweave.categories, modweave.storage_cleanup_tasks cascade")
    visited.clear()
  }

  private fun raw(
      method: String,
      path: String,
      data: ByteArray? = null,
      contentType: String? = null,
      token: String? = null,
      headers: Map<String, String> = emptyMap(),
  ): HttpResponse<ByteArray> {
    val request =
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:$port$path"))
            .timeout(Duration.ofSeconds(30))
    if (contentType != null) request.header("Content-Type", contentType)
    if (token != null) request.header("Authorization", "Bearer $token")
    headers.forEach { (name, value) -> request.header(name, value) }
    request.method(
        method,
        data?.let { HttpRequest.BodyPublishers.ofByteArray(it) }
            ?: HttpRequest.BodyPublishers.noBody())
    val sent = request.build()
    val response = http.send(sent, HttpResponse.BodyHandlers.ofByteArray())
    val uriPath = sent.uri().path
    if (uriPath.startsWith("/api/v2/") &&
        uriPath !in setOf("/api/v2/docs", "/api/v2/openapi.yaml") &&
        !uriPath.startsWith("/api/v2/swagger-ui/")) {
      contract.validate(sent, data, response)
      if (response.statusCode() >= 400) {
        assertEquals(response.statusCode(), mapper.readTree(response.body())["status"].asInt())
      }
    }
    return response
  }

  private fun call(
      id: String,
      expected: Int = 200,
      pathValues: Map<String, String> = emptyMap(),
      query: Map<String, String> = emptyMap(),
      body: Map<String, Any?>? = null,
      token: String? = null,
      multipart: ByteArray? = null,
  ): JsonNode {
    val endpoint = contract.operations.getValue(id)
    val operation = endpoint.operation
    var path = endpoint.path
    pathValues.forEach { (key, value) -> path = path.replace("{$key}", encode(value)) }
    if (query.isNotEmpty()) path += "?" + form(query)
    var contentType: String? = null
    var data: ByteArray? = null
    if (body != null) {
      contentType = operation.requestBody.content.keys.single()
      data =
          if (contentType == "application/json") mapper.writeValueAsBytes(body)
          else form(body).toByteArray()
    }
    if (multipart != null) {
      contentType = "multipart/form-data; boundary=modweave-test-boundary"
      data = multipart
    }
    val response = raw(endpoint.method, path, data, contentType, token)
    assertEquals(expected, response.statusCode(), "$id: ${response.body().decodeToString()}")
    if (endpoint.method == "GET" && expected == 200 && operation.responses.containsKey("304")) {
      if (id != "downloadFile") assertTrue(response.headers().firstValue("Last-Modified").isEmpty)
      else {
        assertTrue(response.headers().firstValue("Last-Modified").isPresent)
        assertTrue(response.headers().firstValue("Link").isPresent)
      }
      val etag = response.headers().firstValue("ETag").orElseThrow()
      val cached = raw("GET", path, token = token, headers = mapOf("If-None-Match" to etag))
      assertEquals(
          304,
          cached.statusCode(),
          "$id must support conditional GET: ${cached.body().decodeToString()}")
      assertEquals(0, cached.body().size, "$id 304 must have no body")
      for (header in listOf("Cache-Control", "ETag", "Last-Modified", "Vary")) {
        assertEquals(
            response.headers().firstValue(header),
            cached.headers().firstValue(header),
            "$id $header")
      }
      assertTrue(response.headers().firstValue("Cache-Control").orElse("").contains("private"))
      assertTrue(response.headers().firstValue("Vary").orElse("").contains("Authorization"))
    }
    visited += id
    if (response.body().isNotEmpty()) {
      val media = response.headers().firstValue("Content-Type").orElse("").substringBefore(';')
      if (media == "application/octet-stream") {
        assertEquals("mod archive", response.body().decodeToString())
        assertTrue(
            response.headers().firstValue("Content-Disposition").orElse("").contains("attachment"))
      } else {
        val value = mapper.readTree(response.body())
        if (expected in 200..299 && id !in setOf("login", "refreshTokens")) validateLinks(value)
        if (expected == 201) {
          val location = response.headers().firstValue("Location").orElseThrow()
          assertTrue(location.startsWith("/api/v2/"), location)
          val retrieved = raw("GET", location, token = token)
          assertEquals(200, retrieved.statusCode(), "$id Location must be readable: $location")
          val stored = mapper.readTree(retrieved.body())
          for (key in listOf("id", "username", "name")) {
            if (value.has(key)) assertEquals(value[key], stored[key], "$id Location identity")
          }
        }
        return value
      }
    }
    if (expected == 204) assertEquals(0, response.body().size)
    return mapper.nullNode()
  }

  private fun encode(value: String): String =
      URLEncoder.encode(value, Charsets.UTF_8).replace("+", "%20")

  private fun form(values: Map<String, Any?>): String =
      values.entries.joinToString("&") { (key, value) ->
        "${encode(key)}=${encode(value.toString())}"
      }

  private fun page() = mapOf("page" to "0", "size" to "20")

  private fun validateLinks(value: JsonNode) {
    val links = value["_links"]
    assertNotNull(links, "Every resource and collection needs hypermedia")
    assertEquals("GET", links["self"]["method"].asText())
    links.properties().forEach { (_, link) ->
      assertTrue(link["href"].asText().startsWith("/api/v2/"))
      assertFalse(link["href"].asText().contains("{"), "Links must be directly usable")
    }
    for (field in listOf("content", "items", "files")) {
      value[field]?.takeIf { it.isArray }?.forEach { validateLinks(it) }
    }
  }

  private fun follow(value: JsonNode, relation: String, token: String? = null): JsonNode {
    val link = value["_links"][relation]
    assertNotNull(link, relation)
    assertEquals("GET", link["method"].asText())
    val response = raw("GET", link["href"].asText(), token = token)
    assertEquals(200, response.statusCode(), "$relation: ${response.body().decodeToString()}")
    return mapper.readTree(response.body())
  }

  private fun register(username: String, admin: Boolean = false): String {
    call(
        "registerUser",
        201,
        body =
            mapOf(
                "username" to username,
                "name" to username,
                "password" to "test-password",
                "email" to "$username@example.com"))
    if (admin) jdbc.update("update modweave.users set is_admin = true where username = ?", username)
    return call("login", body = mapOf("username" to username, "password" to "test-password"))[
            "accessToken"]
        .asText()
  }

  private fun upload(
      fields: Map<String, String>,
      fileField: String,
      filename: String,
      bytes: ByteArray
  ): ByteArray {
    val result = ByteArrayOutputStream()
    fun text(value: String) = result.write(value.toByteArray())
    fields.forEach { (name, value) ->
      text(
          "--modweave-test-boundary\r\nContent-Disposition: form-data; name=\"$name\"\r\n\r\n$value\r\n")
    }
    text(
        "--modweave-test-boundary\r\nContent-Disposition: form-data; name=\"$fileField\"; filename=\"$filename\"\r\nContent-Type: application/octet-stream\r\n\r\n")
    result.write(bytes)
    text("\r\n--modweave-test-boundary--\r\n")
    return result.toByteArray()
  }

  @Test
  fun `category CRUD preserves identity and validates patches and permissions over HTTP`() {
    val admin = register("admin", admin = true)
    val user = register("member")
    val category = mapOf("categoryName" to "Utility")
    call("createCategory", 401, body = mapOf("name" to "Utility"))
    call("createCategory", 403, body = mapOf("name" to "Utility"), token = user)
    call("createCategory", 400, body = mapOf("name" to " "), token = admin)
    call("createCategory", 400, body = mapOf("name" to "x".repeat(51)), token = admin)
    call(
        "createCategory",
        201,
        body = mapOf("name" to "Utility", "description" to "Initial"),
        token = admin)
    call("createCategory", 409, body = mapOf("name" to "utility"), token = admin)
    assertEquals("Initial", call("getCategory", pathValues = category)["description"].asText())
    call("updateCategory", 400, category, body = emptyMap(), token = admin)
    for (description in listOf(42, 0.5, true, emptyList<String>(), emptyMap<String, String>())) {
      call(
          "updateCategory",
          400,
          category,
          body = mapOf("description" to description),
          token = admin)
    }
    call(
        "updateCategory",
        400,
        category,
        body = mapOf("name" to "Renamed", "description" to "x"),
        token = admin)
    call("updateCategory", 403, category, body = mapOf("description" to "x"), token = user)
    val changed =
        call(
            "updateCategory",
            pathValues = category,
            body = mapOf("description" to "Updated"),
            token = admin)
    assertEquals("Utility", changed["name"].asText())
    assertEquals("Updated", call("getCategory", pathValues = category)["description"].asText())
    call(
        "updateCategory", pathValues = category, body = mapOf("description" to null), token = admin)
    assertTrue(call("getCategory", pathValues = category)["description"].isNull)
    call("deleteCategory", 403, category, token = user)
    call("deleteCategory", 204, category, token = admin)
    call("getCategory", 404, category)
    call("deleteCategory", 404, category, token = admin)
    call("listGames", 400, query = mapOf("page" to "-1", "size" to "20"))
    call("getCollection", 400, mapOf("collectionId" to "invalid-uuid"))
    call("listUserMods", 404, mapOf("username" to "missing"), query = page())
    call("listUserCollections", 404, mapOf("username" to "missing"), query = page())
    call("listModVersions", 404, mapOf("modId" to "missing"), query = page())
    call(
        "listCollectionMods",
        404,
        mapOf("collectionId" to "00000000-0000-0000-0000-000000000099"),
        query = page())
    assertEquals(
        415,
        raw("POST", "/api/v2/categories", "name=Utility".toByteArray(), "text/plain", admin)
            .statusCode())
    assertEquals(
        400,
        raw("PATCH", "/api/v2/categories/Utility", "{".toByteArray(), "application/json", admin)
            .statusCode())
    call("login", 401, body = mapOf("username" to "admin", "password" to "wrong-password"))
  }

  @Test
  fun `publication moderation download discussion and collections satisfy the contract`() {
    val admin = register("admin", true)
    val author = register("author")
    val outsider = register("outsider")
    call("listUsers", query = page())
    call("getUser", pathValues = mapOf("username" to "author"))
    val login = call("login", body = mapOf("username" to "author", "password" to "test-password"))
    call("refreshTokens", body = mapOf("refreshToken" to login["refreshToken"].asText()))
    call("updateCurrentUser", body = mapOf("name" to "Updated Author"), token = author)
    assertEquals(
        "Updated Author",
        call("getUser", pathValues = mapOf("username" to "author"))["name"].asText())
    call("updateCurrentUser", 400, body = emptyMap(), token = author)
    call("createCategory", 201, body = mapOf("name" to "Utility"), token = admin)
    call("listCategories")
    call("getCategory", pathValues = mapOf("categoryName" to "Utility"))
    call(
        "updateCategory",
        pathValues = mapOf("categoryName" to "Utility"),
        body = mapOf("description" to "Tools"),
        token = admin)
    val png =
        ByteArrayOutputStream()
            .also {
              javax.imageio.ImageIO.write(
                  java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB),
                  "png",
                  it)
            }
            .toByteArray()
    val game =
        call(
                "createGame",
                201,
                token = admin,
                multipart = upload(mapOf("name" to "Пример + Game"), "image", "game.png", png))[
                "id"]
            .asText()
    call("listGames", query = page())
    val filteredGames = call("listGames", query = page() + ("name" to "Пример + Game"))
    assertEquals(game, filteredGames["content"][0]["id"].asText())
    assertEquals(filteredGames, follow(filteredGames, "last"))
    call("getGame", pathValues = mapOf("gameId" to game))
    val modUpload =
        upload(
                mapOf(
                    "name" to "Пример Mod",
                    "gameId" to game,
                    "versionName" to "1.0",
                    "categories" to "Utility"),
                "image",
                "mod.png",
                png)
            .toString(Charsets.ISO_8859_1)
            .replace("--modweave-test-boundary--\r\n", "")
            .toByteArray(Charsets.ISO_8859_1) +
            upload(emptyMap(), "files", "mod.zip", "mod archive".toByteArray())
    val mod = call("createMod", 201, token = author, multipart = modUpload)["id"].asText()
    val modPath = mapOf("modId" to mod)
    assertEquals(mod, call("listMods", query = page(), token = admin)["content"][0]["id"].asText())
    assertTrue(call("listMods", query = page())["content"].isEmpty)
    val modCard = call("getMod", pathValues = modPath, token = author)
    assertEquals("пример-mod", mod)
    assertEquals(mod, follow(modCard, "versions", author)["content"][0]["modId"].asText())
    assertEquals(
        mod,
        call(
                "listUserMods",
                pathValues = mapOf("username" to "author"),
                query = page(),
                token = author)["content"][0]["id"]
            .asText())
    assertTrue(
        call("listUserMods", pathValues = mapOf("username" to "author"), query = page())["content"]
            .isEmpty)
    val versions = call("listModVersions", pathValues = modPath, query = page(), token = author)
    val version = versions["content"][0]
    assertEquals("PENDING", version["status"].asText())
    val versionPath = modPath + ("versionId" to version["id"].asText())
    val filePath = mapOf("fileId" to version["files"][0]["id"].asText())
    call("getMod", 404, modPath)
    call("getMod", 404, modPath, token = outsider)
    call("listModVersions", 404, modPath, query = page())
    val privateMod = raw("GET", "/api/v2/mods/$mod", token = author)
    assertEquals(
        404,
        raw(
                "GET",
                "/api/v2/mods/$mod",
                headers =
                    mapOf("If-None-Match" to privateMod.headers().firstValue("ETag").orElseThrow()))
            .statusCode())
    call("downloadFile", 403, filePath, token = outsider)
    call("getModVersion", pathValues = versionPath, token = author)
    call("getModVersion", 403, versionPath, token = outsider)
    call("getModVersion", 404, versionPath + ("modId" to "another-mod"), token = author)
    call(
        "getModVersion", 404, versionPath + ("versionId" to "00000000-0000-0000-0000-000000000099"))
    assertEquals(
        415,
        raw(
                "PATCH",
                "/api/v2/mods/$mod/versions/${version["id"].asText()}",
                "status=APPROVED".toByteArray(),
                "application/x-www-form-urlencoded",
                admin)
            .statusCode())
    call("moderateVersion", 403, versionPath, body = mapOf("status" to "APPROVED"), token = author)
    call("moderateVersion", 400, versionPath, body = mapOf("status" to "PENDING"), token = admin)
    call("moderateVersion", 400, versionPath, body = emptyMap(), token = admin)
    for (status in listOf(null, 0, "0", true, "approved")) {
      call("moderateVersion", 400, versionPath, body = mapOf("status" to status), token = admin)
    }
    call(
        "moderateVersion",
        400,
        versionPath,
        body = mapOf("status" to "APPROVED", "name" to "Renamed"),
        token = admin)
    call(
        "moderateVersion",
        pathValues = versionPath,
        body = mapOf("status" to "APPROVED"),
        token = admin)
    call("getModVersion", pathValues = versionPath)
    call("downloadFile", pathValues = filePath)
    val fileUrl = version["files"][0]["_links"]["download"]["href"].asText()
    val downloaded = raw("GET", fileUrl)
    val downloads =
        jdbc.queryForObject(
            "select downloads from modweave.mod_files where id = ?::uuid",
            Int::class.java,
            filePath["fileId"])
    val cachedDownload =
        raw(
            "GET",
            fileUrl,
            headers =
                mapOf("If-None-Match" to downloaded.headers().firstValue("ETag").orElseThrow()))
    assertEquals(304, cachedDownload.statusCode())
    assertEquals(
        downloads,
        jdbc.queryForObject(
            "select downloads from modweave.mod_files where id = ?::uuid",
            Int::class.java,
            filePath["fileId"]))
    assertEquals(200, raw("GET", "/api/v1/files/${filePath["fileId"]}/download").statusCode())
    assertEquals(
        200,
        raw(
                "PATCH",
                "/api/v1/mods/$mod/versions/${version["id"].asText()}?status=APPROVED",
                token = admin)
            .statusCode())
    val anotherVersion =
        call(
            "createVersion",
            201,
            modPath,
            token = author,
            multipart =
                upload(mapOf("name" to "1.1"), "files", "mod.zip", "mod archive".toByteArray()))
    val comment =
        call(
            "createComment",
            201,
            body = mapOf("content" to "Works well", "modId" to mod),
            token = author)
    val commentPath = mapOf("commentId" to comment["id"].asText())
    assertEquals(mod, follow(comment, "mod")["id"].asText())
    assertEquals("author", follow(comment, "author")["username"].asText())
    assertEquals(comment["id"], follow(modCard, "comments")["content"][0]["id"])
    call("getComment", pathValues = commentPath)
    assertEquals(
        1, call("listModComments", query = page() + ("modId" to mod))["totalElements"].asInt())
    call("deleteComment", 403, commentPath, token = outsider)
    val collection =
        call("createCollection", 201, body = mapOf("name" to "Favorites"), token = author)
    val collectionPath = mapOf("collectionId" to collection["id"].asText())
    assertTrue(follow(collection, "mods")["content"].isEmpty)
    call("getCollection", pathValues = collectionPath)
    call("listUserCollections", pathValues = mapOf("username" to "author"), query = page())
    assertEquals(
        415,
        raw(
                "POST",
                "/api/v2/collections/${collection["id"].asText()}/mods",
                "modId=$mod".toByteArray(),
                "application/x-www-form-urlencoded",
                author)
            .statusCode())
    call("addCollectionMod", 403, collectionPath, body = mapOf("modId" to mod), token = outsider)
    for (body in
        listOf(
            emptyMap(),
            mapOf("modId" to mod, "index" to -1),
            mapOf("modId" to mod, "index" to 0.5),
            mapOf("modId" to mod, "index" to "0"),
            mapOf("modId" to mod, "index" to true),
            mapOf("modId" to mod, "index" to null),
            mapOf("modId" to mod, "extra" to true),
            mapOf("modId" to 123))) {
      call("addCollectionMod", 400, collectionPath, body = body, token = author)
    }
    call(
        "addCollectionMod",
        pathValues = collectionPath,
        body = mapOf("modId" to mod),
        token = author)
    call("addCollectionMod", 400, collectionPath, body = mapOf("modId" to mod), token = author)
    assertEquals(
        mod,
        call("listCollectionMods", pathValues = collectionPath, query = page())["content"][0]["id"]
            .asText())
    call("removeCollectionMod", 204, pathValues = collectionPath + ("modId" to mod), token = author)
    call("removeCollectionMod", 204, pathValues = collectionPath + ("modId" to mod), token = author)
    call(
        "moderateVersion",
        pathValues = versionPath,
        body = mapOf("status" to "REJECTED"),
        token = admin)
    assertEquals(
        403,
        raw(
                "GET",
                fileUrl,
                headers =
                    mapOf("If-None-Match" to downloaded.headers().firstValue("ETag").orElseThrow()))
            .statusCode())
    call("getMod", 404, modPath)
    call("getMod", pathValues = modPath, token = author)
    call("getComment", 404, commentPath)
    assertTrue(call("listModComments", query = page() + ("modId" to mod))["content"].isEmpty)
    call(
        "createComment",
        404,
        body = mapOf("content" to "Hidden mod", "modId" to mod),
        token = outsider)
    call(
        "moderateVersion",
        pathValues = versionPath,
        body = mapOf("status" to "APPROVED"),
        token = admin)
    call("deleteCollection", 204, pathValues = collectionPath, token = author)
    call("getCollection", 404, pathValues = collectionPath)
    call("deleteComment", 204, pathValues = commentPath, token = author)
    call("getComment", 404, pathValues = commentPath)
    call(
        "deleteVersion",
        204,
        pathValues = modPath + ("versionId" to anotherVersion["id"].asText()),
        token = author)
    call(
        "getModVersion", 404, pathValues = modPath + ("versionId" to anotherVersion["id"].asText()))
    call("deleteMod", 204, pathValues = modPath, token = author)
    call("getMod", 404, pathValues = modPath)
    call("deleteGame", 204, pathValues = mapOf("gameId" to game), token = admin)
    call("getGame", 404, pathValues = mapOf("gameId" to game))
    call("deleteCategory", 204, mapOf("categoryName" to "Utility"), token = admin)
    assertEquals(
        contract.operations.keys,
        visited,
        "Every declared operation must run against the real server")
  }

  @Test
  fun `hypermedia pagination and conditional requests remain usable after changes`() {
    val admin = register("admin", true)
    val alpha = register("alpha")
    register("alpine")
    call("updateCurrentUser", body = mapOf("name" to "Different display name"), token = alpha)
    val firstResponse =
        raw("GET", "/api/v2/users?page=0&size=1&username=al&sort=name,asc&sort=username,desc")
    assertEquals(200, firstResponse.statusCode())
    val first = mapper.readTree(firstResponse.body())
    validateLinks(first)
    assertEquals(
        2, first["totalElements"].asInt(), "username filters the login, not the display name")
    assertFalse(first["_links"].has("previous"))
    val nextUrl = first["_links"]["next"]["href"].asText()
    assertTrue(nextUrl.contains("username=al"))
    assertTrue(nextUrl.contains("sort=name,asc&sort=username,desc"))
    val second = follow(first, "next")
    assertEquals(1, second["page"].asInt())
    assertFalse(second["_links"].has("next"))
    assertNotEquals(first["content"][0]["username"], second["content"][0]["username"])
    assertEquals(first, follow(second, "previous"))
    val user = follow(first["content"][0], "self")
    assertTrue(follow(user, "mods")["content"].isEmpty)
    assertTrue(follow(user, "collections")["content"].isEmpty)
    val capped = call("listUsers", query = mapOf("page" to "0", "size" to "9999"))
    assertEquals(100, capped["size"].asInt())
    assertTrue(capped["_links"]["self"]["href"].asText().contains("size=100"))
    val empty = call("listUsers", query = page() + ("username" to "missing"))
    assertFalse(empty["_links"].has("next"))
    assertEquals(empty, follow(empty, "last"))
    call("listUsers", 400, query = page() + ("sort" to "password,asc"))
    call("listGames", 400, query = page() + ("sort" to "missing,asc"))
    call("listMods", 400, query = page() + ("sort" to "publisher.password,asc"))

    val category =
        call("createCategory", 201, body = mapOf("name" to "Моды + утилиты"), token = admin)
    val self = category["_links"]["self"]["href"].asText()
    assertEquals("Моды + утилиты", follow(category, "self")["name"].asText())
    assertEquals("PATCH", category["_links"]["update"]["method"].asText())
    val guestResponse = raw("GET", self)
    val guest = mapper.readTree(guestResponse.body())
    assertFalse(guest["_links"].has("update"))
    assertFalse(guest["_links"].has("delete"))
    val etag = guestResponse.headers().firstValue("ETag").orElseThrow()
    for (validator in listOf(etag, etag.removePrefix("W/"), "\"unrelated\", $etag", "*")) {
      val cached = raw("GET", self, headers = mapOf("If-None-Match" to validator))
      assertEquals(
          304,
          cached.statusCode(),
          "Validator: $validator, response: ${cached.body().decodeToString()}")
      assertEquals(0, cached.body().size)
    }
    assertEquals(
        200,
        raw("GET", self, token = admin, headers = mapOf("If-None-Match" to etag)).statusCode(),
        "Admin representation has additional action links")
    call(
        "updateCategory",
        pathValues = mapOf("categoryName" to "Моды + утилиты"),
        body = mapOf("description" to "Changed"),
        token = admin)
    val changed = raw("GET", self, headers = mapOf("If-None-Match" to etag))
    assertEquals(200, changed.statusCode())
    assertNotEquals(etag, changed.headers().firstValue("ETag").orElseThrow())
    assertEquals("Changed", mapper.readTree(changed.body())["description"].asText())
    call("deleteCategory", 204, mapOf("categoryName" to "Моды + утилиты"), token = admin)
    assertEquals(404, raw("GET", self, headers = mapOf("If-None-Match" to "*")).statusCode())
  }

  @Test
  fun `multipart validation reports client errors for corrupt or oversized images`() {
    val admin = register("admin", true)
    val corrupt =
        java.util.Base64.getDecoder()
            .decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Wl6sEAAAAAASUVORK5CYII=")
    call(
        "createGame",
        400,
        token = admin,
        multipart = upload(mapOf("name" to "Invalid"), "image", "corrupt.png", corrupt))
    call(
        "createGame",
        413,
        token = admin,
        multipart = upload(mapOf("name" to "Too large"), "image", "large.png", ByteArray(2048)))
  }

  @Test
  fun `Spring resource server rejects invalid bearer tokens and uses current roles`() {
    val anonymous = raw("POST", "/api/v2/categories", "{}".toByteArray(), "application/json")
    assertEquals(401, anonymous.statusCode())
    assertTrue(anonymous.headers().firstValue("WWW-Authenticate").orElse("").startsWith("Bearer"))
    val access = register("member")
    val tokens = call("login", body = mapOf("username" to "member", "password" to "test-password"))
    for (token in listOf("invalid", tokens["refreshToken"].asText())) {
      val response = raw("GET", "/api/v2/categories", token = token)
      assertEquals(401, response.statusCode())
      assertTrue(response.headers().firstValue("WWW-Authenticate").orElse("").startsWith("Bearer"))
    }
    call("refreshTokens", 401, body = mapOf("refreshToken" to access))
    jdbc.update("update modweave.users set is_admin = true where username = 'member'")
    call("createCategory", 201, body = mapOf("name" to "RoleCheck"), token = access)
    jdbc.update("update modweave.users set is_admin = false where username = 'member'")
    call("deleteCategory", 403, mapOf("categoryName" to "RoleCheck"), token = access)
    jdbc.update("delete from modweave.users where username = 'member'")
    assertEquals(401, raw("GET", "/api/v2/categories", token = access).statusCode())
  }

  @Test
  fun `v1 remains usable and Swagger serves the authoritative contract and local assets`() {
    val admin = register("admin", true)
    val legacy =
        raw(
            "POST",
            "/api/v1/categories",
            "name=Legacy&description=FromV1".toByteArray(),
            "application/x-www-form-urlencoded",
            admin)
    assertEquals(201, legacy.statusCode())
    assertEquals(200, raw("GET", "/api/v1/categories").statusCode())
    val v1Categories = mapper.readTree(raw("GET", "/api/v1/categories").body())
    assertTrue(v1Categories.isArray)
    assertFalse(v1Categories[0].has("_links"))
    assertEquals(
        "FromV1",
        call("getCategory", pathValues = mapOf("categoryName" to "Legacy"))["description"].asText())
    call(
        "updateCategory",
        pathValues = mapOf("categoryName" to "Legacy"),
        body = mapOf("description" to "FromV2"),
        token = admin)
    assertTrue(raw("GET", "/api/v1/categories").body().decodeToString().contains("FromV2"))
    assertEquals(
        200,
        raw(
                "PATCH",
                "/api/v1/categories",
                "name=Legacy&description=StillV1".toByteArray(),
                "application/x-www-form-urlencoded",
                admin)
            .statusCode())
    assertEquals(
        200, raw("DELETE", "/api/v1/categories?category=Legacy", token = admin).statusCode())
    assertEquals(401, raw("DELETE", "/api/v1/games/missing").statusCode())
    assertEquals(400, raw("GET", "/api/v3/categories").statusCode())
    val docs = raw("GET", "/api/v2/docs")
    assertEquals(200, docs.statusCode())
    assertTrue(docs.body().decodeToString().contains("/api/v2/openapi.yaml"))
    val yaml = raw("GET", "/api/v2/openapi.yaml")
    assertEquals(200, yaml.statusCode())
    assertArrayEquals(
        javaClass.getResourceAsStream("/static/api/v2/openapi.yaml")!!.readBytes(), yaml.body())
    for (asset in listOf("swagger-ui.css", "swagger-ui-bundle.js")) {
      assertEquals(200, raw("GET", "/api/v2/swagger-ui/$asset").statusCode())
    }
  }

  @Test
  fun `OpenAPI validator rejects broken status headers and bodies from a real response`() {
    val admin = register("admin", true)
    call("createCategory", 201, body = mapOf("name" to "Utility"), token = admin)
    val valid = call("getCategory", pathValues = mapOf("categoryName" to "Utility"))
    fun report(body: JsonNode, status: Int = 200, mediaType: String = "application/json") =
        contract.validator.validateResponse(
            "/api/v2/categories/Utility",
            Request.Method.GET,
            SimpleResponse.Builder(status)
                .withContentType(mediaType)
                .withHeader("Cache-Control", "private, no-cache")
                .withHeader("ETag", "\"test\"")
                .withBody(mapper.writeValueAsBytes(body))
                .build())
    contract.assertValid(report(valid))
    for (field in listOf("name", "_links")) {
      val broken =
          valid.deepCopy<com.fasterxml.jackson.databind.node.ObjectNode>().apply { remove(field) }
      assertTrue(report(broken).hasErrors(), "Missing $field must violate the contract")
    }
    val wrongType =
        valid.deepCopy<com.fasterxml.jackson.databind.node.ObjectNode>().apply {
          put("description", 42)
        }
    assertTrue(report(wrongType).hasErrors(), "OpenAPI 3.1 string/null must reject a number")
    assertTrue(report(valid, status = 418).hasErrors(), "Undeclared status must be rejected")
    assertTrue(
        report(valid, mediaType = "text/plain").hasErrors(), "Wrong content type must be rejected")
    val brokenLink = valid.deepCopy<com.fasterxml.jackson.databind.node.ObjectNode>()
    (brokenLink["_links"]["self"] as com.fasterxml.jackson.databind.node.ObjectNode).put(
        "extra", true)
    assertTrue(
        report(brokenLink).hasErrors(), "Explicit additionalProperties false must be enforced")
    val extended =
        valid.deepCopy<com.fasterxml.jackson.databind.node.ObjectNode>().put("extra", true)
    contract.assertValid(report(extended))

    val missingLocation =
        contract.validator.validateResponse(
            "/api/v2/categories",
            Request.Method.POST,
            SimpleResponse.Builder(201)
                .withContentType("application/json")
                .withBody(mapper.writeValueAsBytes(valid))
                .build())
    assertTrue(missingLocation.hasErrors(), "Required Location must be checked")
    val missingCacheHeaders =
        contract.validator.validateResponse(
            "/api/v2/categories/Utility",
            Request.Method.GET,
            SimpleResponse.Builder(200)
                .withContentType("application/json")
                .withBody(mapper.writeValueAsBytes(valid))
                .build())
    assertTrue(missingCacheHeaders.hasErrors(), "Required cache headers must be checked")
    val invalidPage =
        contract.validator.validateRequest(
            SimpleRequest.Builder.get("/api/v2/users")
                .withQueryParam("page", "-1")
                .withQueryParam("size", "20")
                .withQueryParam("sort", "name,asc", "username,desc")
                .build())
    assertTrue(invalidPage.hasErrors(), "Query constraints must be checked")
  }
}
