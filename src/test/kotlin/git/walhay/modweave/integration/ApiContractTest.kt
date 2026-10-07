package git.walhay.modweave.integration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import git.walhay.modweave.testutils.StorageTestInfrastructure
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.Base64
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.yaml.snakeyaml.Yaml

/** Real HTTP -> security -> controller -> service -> repository -> PostgreSQL/S3, without mocks. */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties =
        [
            "spring.cache.type=none",
            "storage.cleanup.enabled=false",
            "security.jwt.secret=ZmFrZS1zZWNyZXQtc2VjdXJlLWtleS1mb3ItanVzdC1kZXZlbG9wbWVudA==",
        ])
class ApiContractTest {
  companion object {
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
  private val contract: JsonNode =
      mapper.valueToTree(
          Yaml().load<Any>(javaClass.getResourceAsStream("/static/api/v2/openapi.yaml")))
  private val schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
  private val visited = mutableSetOf<String>()

  @BeforeEach
  fun resetFixtures() {
    jdbc.execute(
        "truncate modweave.users, modweave.games, modweave.categories, modweave.storage_cleanup_tasks cascade")
    visited.clear()
  }

  private fun resolve(node: JsonNode): JsonNode =
      if (node.has("\$ref")) contract.at(node["\$ref"].asText().removePrefix("#")) else node

  private fun validateResponse(schema: JsonNode, value: JsonNode) {
    val root = mapper.createObjectNode()
    root.set<JsonNode>("components", contract["components"])
    root.set<JsonNode>("allOf", mapper.createArrayNode().add(schema))
    val errors = schemaFactory.getSchema(root).validate(value)
    assertTrue(errors.isEmpty(), errors.joinToString("\n"))
  }

  private fun raw(
      method: String,
      path: String,
      data: ByteArray? = null,
      contentType: String? = null,
      token: String? = null,
  ): HttpResponse<ByteArray> {
    val request =
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:$port$path"))
            .timeout(Duration.ofSeconds(30))
    if (contentType != null) request.header("Content-Type", contentType)
    if (token != null) request.header("Authorization", "Bearer $token")
    request.method(
        method,
        data?.let { HttpRequest.BodyPublishers.ofByteArray(it) }
            ?: HttpRequest.BodyPublishers.noBody())
    return http.send(request.build(), HttpResponse.BodyHandlers.ofByteArray())
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
    val pathEntry =
        contract["paths"].properties().first { (_, item) ->
          item.properties().any { (_, op) -> op["operationId"].asText() == id }
        }
    val operationEntry =
        pathEntry.value.properties().first { (_, op) -> op["operationId"].asText() == id }
    val operation = operationEntry.value
    var path = pathEntry.key
    pathValues.forEach { (key, value) -> path = path.replace("{$key}", encode(value)) }
    if (query.isNotEmpty()) path += "?" + form(query)
    var contentType: String? = null
    var data: ByteArray? = null
    if (body != null) {
      contentType = operation["requestBody"]["content"].fieldNames().next()
      data =
          if (contentType == "application/json") mapper.writeValueAsBytes(body)
          else form(body).toByteArray()
    }
    if (multipart != null) {
      contentType = "multipart/form-data; boundary=modweave-test-boundary"
      data = multipart
    }
    val response = raw(operationEntry.key.uppercase(), path, data, contentType, token)
    assertEquals(expected, response.statusCode(), "$id: ${response.body().decodeToString()}")
    val declared = operation["responses"][expected.toString()]
    assertNotNull(declared, "$id does not declare $expected")
    val responseContract = resolve(declared)
    responseContract["headers"]?.properties()?.forEach { (name, header) ->
      if (header["required"]?.asBoolean() == true)
          assertTrue(response.headers().firstValue(name).isPresent, name)
    }
    visited += id
    if (responseContract.has("content")) {
      val media = response.headers().firstValue("Content-Type").orElse("").substringBefore(';')
      assertTrue(responseContract["content"].has(media), "$id: undeclared Content-Type $media")
      if (media == "application/octet-stream") {
        assertEquals("mod archive", response.body().decodeToString())
        assertTrue(
            response.headers().firstValue("Content-Disposition").orElse("").contains("attachment"))
      } else {
        val value = mapper.readTree(response.body())
        validateResponse(responseContract["content"][media]["schema"], value)
        if (expected == 201) {
          val location = response.headers().firstValue("Location").orElseThrow()
          assertTrue(location.startsWith("/api/v2/"), location)
          val retrieved = raw("GET", location, token = token)
          assertEquals(200, retrieved.statusCode(), "$id Location must be readable: $location")
          val stored = mapper.readTree(retrieved.body())
          validateResponse(responseContract["content"][media]["schema"], stored)
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

  private fun register(username: String, admin: Boolean = false): String {
    call(
        "registerUser",
        201,
        body =
            mapOf(
                "username" to username,
                "name" to username,
                "password" to "test-password",
                "email" to "$username@example.test"))
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
    call("updateCategory", 400, category, body = mapOf("description" to 42), token = admin)
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
        Base64.getDecoder()
            .decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Wl6sEAAAAAASUVORK5CYII=")
    val game =
        call(
                "createGame",
                201,
                token = admin,
                multipart = upload(mapOf("name" to "Example Game"), "image", "game.png", png))["id"]
            .asText()
    call("listGames", query = page())
    call("getGame", pathValues = mapOf("gameId" to game))
    val modUpload =
        upload(
                mapOf(
                    "name" to "Example Mod",
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
    call("getMod", pathValues = modPath, token = author)
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
    assertTrue(call("listModVersions", pathValues = modPath, query = page())["content"].isEmpty)
    call("downloadFile", 403, filePath, token = outsider)
    call("getModVersion", pathValues = versionPath, token = author)
    call("getModVersion", 403, versionPath, token = outsider)
    call("getModVersion", 404, versionPath + ("modId" to "another-mod"), token = author)
    call(
        "getModVersion", 404, versionPath + ("versionId" to "00000000-0000-0000-0000-000000000099"))
    call("moderateVersion", 403, versionPath, body = mapOf("status" to "APPROVED"), token = author)
    call("moderateVersion", 400, versionPath, body = mapOf("status" to "PENDING"), token = admin)
    call("moderateVersion", 400, versionPath, body = emptyMap(), token = admin)
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
    call("getComment", pathValues = commentPath)
    assertEquals(
        1, call("listModComments", query = page() + ("modId" to mod))["totalElements"].asInt())
    call("deleteComment", 403, commentPath, token = outsider)
    val collection =
        call("createCollection", 201, body = mapOf("name" to "Favorites"), token = author)
    val collectionPath = mapOf("collectionId" to collection["id"].asText())
    call("getCollection", pathValues = collectionPath)
    call("listUserCollections", pathValues = mapOf("username" to "author"), query = page())
    call("addCollectionMod", 403, collectionPath, mapOf("modId" to mod), token = outsider)
    call(
        "addCollectionMod",
        pathValues = collectionPath,
        query = mapOf("modId" to mod),
        token = author)
    call("addCollectionMod", 400, collectionPath, mapOf("modId" to mod), token = author)
    assertEquals(
        mod,
        call("listCollectionMods", pathValues = collectionPath, query = page())["content"][0]["id"]
            .asText())
    call("removeCollectionMod", 204, pathValues = collectionPath + ("modId" to mod), token = author)
    call("removeCollectionMod", 204, pathValues = collectionPath + ("modId" to mod), token = author)
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
    val allOperations =
        contract["paths"]
            .properties()
            .flatMap { (_, item) ->
              item.properties().map { (_, op) -> op["operationId"].asText() }
            }
            .toSet()
    assertEquals(
        allOperations, visited, "Every declared operation must run against the real server")
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
  fun `schema validation detects a missing required field in an actual server response`() {
    val admin = register("admin", true)
    call("createCategory", 201, body = mapOf("name" to "Utility"), token = admin)
    val valid = call("getCategory", pathValues = mapOf("categoryName" to "Utility"))
    val broken =
        valid.deepCopy<com.fasterxml.jackson.databind.node.ObjectNode>().apply { remove("name") }
    val schema = mapper.readTree("""{"${'$'}ref":"#/components/schemas/Category"}""")
    assertThrows(AssertionError::class.java) { validateResponse(schema, broken) }
    validateResponse(schema, valid)
  }
}
