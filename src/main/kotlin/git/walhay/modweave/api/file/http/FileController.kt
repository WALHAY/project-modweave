package git.walhay.modweave.api.file.http

import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/files")
class FileController(
    @param:Value($$"${minio.endpoint}") private val minioUrl: String,
) {
  @GetMapping("/{bucket}/{fileId}/download")
  fun downloadFile(
      @PathVariable bucket: String,
      @PathVariable fileId: Long,
      response: HttpServletResponse,
  ) {
    response.sendRedirect(null) // FIXME: change location to file path
  }
}
