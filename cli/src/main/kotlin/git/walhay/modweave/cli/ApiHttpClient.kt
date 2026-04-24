package git.walhay.modweave.cli

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Base64
import kotlin.io.path.name

class ApiHttpClient {
  private val client = HttpClient.newHttpClient()

  fun get(global: GlobalOptions, path: String, query: Map<String, String?> = emptyMap()): String {
    val uri = buildUri(global, path, query)
    val request = baseRequest(global, uri).GET().build()
    return send(request)
  }

  fun put(global: GlobalOptions, path: String, query: Map<String, String?> = emptyMap()): String {
    val uri = buildUri(global, path, query)
    val request = baseRequest(global, uri).PUT(HttpRequest.BodyPublishers.noBody()).build()
    return send(request)
  }

  fun delete(global: GlobalOptions, path: String, query: Map<String, String?> = emptyMap()): String {
    val uri = buildUri(global, path, query)
    val request = baseRequest(global, uri).DELETE().build()
    return send(request)
  }

  fun formRequest(
      global: GlobalOptions,
      method: String,
      path: String,
      form: Map<String, String?>,
  ): String {
    val content =
        form.entries
            .filter { !it.value.isNullOrBlank() }
            .joinToString("&") { "${urlEncode(it.key)}=${urlEncode(it.value!!)}" }
    val uri = buildUri(global, path, emptyMap())
    val request =
        baseRequest(global, uri)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .method(method, HttpRequest.BodyPublishers.ofString(content))
            .build()
    return send(request)
  }

  fun multipartRequest(
      global: GlobalOptions,
      method: String,
      path: String,
      fields: Map<String, String?>,
      files: Map<String, List<Path>>,
  ): String {
    val boundary = "modweave-cli-${System.currentTimeMillis()}"
    val body = buildMultipartBody(boundary, fields, files)
    val uri = buildUri(global, path, emptyMap())
    val request =
        baseRequest(global, uri)
            .header("Content-Type", "multipart/form-data; boundary=$boundary")
            .method(method, HttpRequest.BodyPublishers.ofByteArray(body))
            .build()
    return send(request)
  }

  private fun buildMultipartBody(
      boundary: String,
      fields: Map<String, String?>,
      files: Map<String, List<Path>>,
  ): ByteArray {
    val out = mutableListOf<Byte>()

    fun write(bytes: ByteArray) {
      out.addAll(bytes.toList())
    }

    for ((name, value) in fields) {
      if (value.isNullOrBlank()) continue
      write("--$boundary\r\n".toByteArray(StandardCharsets.UTF_8))
      write("Content-Disposition: form-data; name=\"$name\"\r\n\r\n".toByteArray(StandardCharsets.UTF_8))
      write("$value\r\n".toByteArray(StandardCharsets.UTF_8))
    }

    for ((name, paths) in files) {
      for (path in paths) {
        require(Files.exists(path) && Files.isRegularFile(path)) { "File does not exist: $path" }

        val contentType = Files.probeContentType(path) ?: "application/octet-stream"
        write("--$boundary\r\n".toByteArray(StandardCharsets.UTF_8))
        write(
            "Content-Disposition: form-data; name=\"$name\"; filename=\"${path.name}\"\r\n"
                .toByteArray(StandardCharsets.UTF_8))
        write("Content-Type: $contentType\r\n\r\n".toByteArray(StandardCharsets.UTF_8))
        write(Files.readAllBytes(path))
        write("\r\n".toByteArray(StandardCharsets.UTF_8))
      }
    }

    write("--$boundary--\r\n".toByteArray(StandardCharsets.UTF_8))
    return out.toByteArray()
  }

  private fun baseRequest(global: GlobalOptions, uri: URI): HttpRequest.Builder {
    val builder = HttpRequest.newBuilder(uri).header("Accept", "application/json")
    if (global.username != null || global.password != null) {
      require(!(global.username.isNullOrBlank() || global.password.isNullOrBlank())) {
        "Both --username and --password are required for authenticated commands."
      }
      val auth = Base64.getEncoder().encodeToString("${global.username}:${global.password}".toByteArray())
      builder.header("Authorization", "Basic $auth")
    }
    return builder
  }

  private fun buildUri(global: GlobalOptions, path: String, query: Map<String, String?>): URI {
    val base = global.baseUrl.trimEnd('/')
    val queryString =
        query.entries
            .filter { !it.value.isNullOrBlank() }
            .joinToString("&") { "${urlEncode(it.key)}=${urlEncode(it.value!!)}" }

    val fullPath = "$base/api/v1${if (path.startsWith("/")) path else "/$path"}"
    return URI.create(if (queryString.isBlank()) fullPath else "$fullPath?$queryString")
  }

  private fun send(request: HttpRequest): String {
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    val body = response.body().takeIf { it.isNotBlank() } ?: "<empty>"
    return "HTTP ${response.statusCode()}\n$body"
  }

  private fun urlEncode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
}
