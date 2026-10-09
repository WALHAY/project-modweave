package git.walhay.modweave.api.common.http

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.security.MessageDigest
import java.util.HexFormat
import org.springframework.web.context.request.ServletWebRequest

/**
 * Tracks when a JSON representation was last observed to change, without caching response bodies.
 */
class RepresentationValidators {
  private data class Validator(val etag: String, val modified: Long)

  private val observed =
      object : LinkedHashMap<String, Validator>(128, 0.75f, true) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<String, Validator>?
        ): Boolean = size > 1024
      }

  fun check(
      request: HttpServletRequest,
      response: HttpServletResponse,
      key: String,
      etag: String
  ): Boolean {
    val modified =
        synchronized(observed) {
          val old = observed[key]
          val current = if (old?.etag == etag) old else Validator(etag, System.currentTimeMillis())
          observed[key] = current
          current.modified
        }
    return check(request, response, etag, modified)
  }

  companion object {
    fun etag(bytes: ByteArray): String =
        "W/\"${HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))}\""

    fun check(
        request: HttpServletRequest,
        response: HttpServletResponse,
        etag: String,
        modified: Long
    ): Boolean {
      // Revalidation runs after authorization, including when a version's visibility changes.
      response.setHeader("Cache-Control", "private, no-cache")
      response.setHeader("Vary", "Authorization")
      response.setHeader("ETag", etag)
      response.setDateHeader("Last-Modified", modified)
      // Only ETag is authoritative: legacy mutable entities have no persisted update timestamp.
      return ServletWebRequest(request, response).checkNotModified(etag)
    }
  }
}
