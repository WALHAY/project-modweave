package git.walhay.modweave.cli

import git.walhay.modweave.api.category.CategoryId
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.temporal.Temporal
import java.util.UUID
import kotlin.io.path.name
import kotlin.reflect.full.memberProperties
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.web.multipart.MultipartFile

abstract class ShellCommandSupport {
  protected fun parseSort(sort: String): Sort {
    val parts = sort.split(",").map { it.trim() }
    val property = parts.firstOrNull().orEmpty()
    require(property.isNotBlank()) { "Sort property is required. Example: name,asc" }
    val direction =
        if (parts.getOrNull(1).equals("desc", ignoreCase = true)) {
          Sort.Direction.DESC
        } else {
          Sort.Direction.ASC
        }
    return Sort.by(direction, property)
  }

  protected fun categoryIds(raw: String?): Set<CategoryId> =
      raw
          ?.split(",")
          ?.map { it.trim() }
          ?.filter { it.isNotBlank() }
          ?.map { CategoryId(it) }
          ?.toSet()
          ?: emptySet()

  protected fun multipartFiles(raw: String): List<MultipartFile> =
      raw.split(",").map { it.trim() }.filter { it.isNotBlank() }.map { multipartFile(Path.of(it)) }

  protected fun multipartFile(path: Path): MultipartFile {
    require(Files.exists(path) && Files.isRegularFile(path)) { "File does not exist: $path" }
    return PathMultipartFile(path)
  }

  protected fun renderPage(page: Page<*>): Map<String, Any?> =
      linkedMapOf(
          "page" to page.number,
          "size" to page.size,
          "totalElements" to page.totalElements,
          "totalPages" to page.totalPages,
          "content" to page.content.map { renderNullable(it) },
      )

  protected fun renderValue(value: Any): Any = renderNullable(value) ?: value.toString()

  protected fun renderNullable(
      value: Any?,
      visited: MutableSet<Int> = mutableSetOf(),
  ): Any? {
    if (value == null) return null
    if (value is String || value is Number || value is Boolean || value is Enum<*>) return value
    if (value is UUID || value is Temporal) return value.toString()
    if (value is Collection<*>) return value.map { renderNullable(it, visited) }
    if (value is Map<*, *>) {
      return value.entries.associate { (key, entryValue) ->
        renderNullable(key, visited) to renderNullable(entryValue, visited)
      }
    }

    val identity = System.identityHashCode(value)
    if (!visited.add(identity)) return value.toString()

    return try {
      val properties = value::class.memberProperties
      if (properties.size == 1 && properties.first().name == "value") {
        renderNullable(properties.first().getter.call(value), visited)
      } else {
        properties.associate { property ->
          property.name to renderNullable(property.getter.call(value), visited)
        }
      }
    } finally {
      visited.remove(identity)
    }
  }
}

private class PathMultipartFile(
    private val path: Path,
) : MultipartFile {
  private val rawBytes: ByteArray by lazy { Files.readAllBytes(path) }
  private val resolvedContentType: String by lazy {
    Files.probeContentType(path) ?: "application/octet-stream"
  }

  override fun getName(): String = path.name

  override fun getOriginalFilename(): String = path.name

  override fun getContentType(): String = resolvedContentType

  override fun isEmpty(): Boolean = getSize() == 0L

  override fun getSize(): Long = rawBytes.size.toLong()

  override fun getBytes(): ByteArray = rawBytes

  override fun getInputStream(): InputStream = Files.newInputStream(path)

  override fun transferTo(dest: File) {
    Files.copy(path, dest.toPath())
  }
}
