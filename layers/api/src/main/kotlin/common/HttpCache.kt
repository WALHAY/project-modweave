package git.walhay.modweave.api.common

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.security.MessageDigest
import java.util.HexFormat
import org.springframework.http.ETag
import org.springframework.http.HttpHeaders

/** Conditional reads are evaluated only after the controller has checked resource access. */
object HttpCache {
  fun etag(bytes: ByteArray): String =
      "W/\"${HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))}\""

  fun check(
      request: HttpServletRequest,
      response: HttpServletResponse,
      etag: String,
      lastModified: Long? = null
  ): Boolean {
    response.setHeader(HttpHeaders.CACHE_CONTROL, "private, no-cache")
    response.addHeader(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
    response.setHeader(HttpHeaders.ETAG, etag)
    lastModified?.let { response.setDateHeader(HttpHeaders.LAST_MODIFIED, it) }
    val current = ETag.create(etag)
    val matches =
        request
            .getHeaders(HttpHeaders.IF_NONE_MATCH)
            .toList()
            .flatMap { ETag.parse(it) }
            .any { it.isWildcard || it.compare(current, false) }
    if (matches) response.status = HttpServletResponse.SC_NOT_MODIFIED
    return matches
  }
}
