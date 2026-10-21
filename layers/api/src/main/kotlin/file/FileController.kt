package git.walhay.modweave.api.file

import git.walhay.modweave.api.common.HttpCache
import git.walhay.modweave.api.common.apiLink
import git.walhay.modweave.domain.file.FileId
import git.walhay.modweave.domain.file.IFileDownloadService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.nio.charset.StandardCharsets
import java.time.ZoneId
import java.util.UUID
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.ContentDisposition
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/files")
class FileController(
    private val downloads: IFileDownloadService,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @GetMapping(value = ["/{fileId}/download", "/mods/{fileId}/download"], version = "1")
  fun downloadFile(
      @PathVariable fileId: UUID,
      response: HttpServletResponse,
  ) = download(fileId, response)

  private fun download(
      fileId: UUID,
      response: HttpServletResponse,
      request: HttpServletRequest? = null
  ) {
    logger.info { "GET /files/$fileId/download" }
    val download = downloads.getDownload(FileId(fileId))
    val file = download.file

    if (request != null) {
      response.setHeader("Link", apiLink(FileController::getFile, fileId).link.toString())
      // Storage keys are unique and immutable; download counters do not change file contents.
      val etag = HttpCache.etag("${file.id}:${file.filePath}:${file.filename}".toByteArray())
      val modified = download.uploadedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
      if (HttpCache.check(request, response, etag, modified)) return
    }

    response.contentType = "application/octet-stream"
    response.setHeader(
        "Content-Disposition",
        ContentDisposition.attachment()
            .filename(file.filename, StandardCharsets.UTF_8)
            .build()
            .toString(),
    )

    downloads.writeDownload(download, response.outputStream)
  }

  @GetMapping("/{fileId}", version = "2")
  fun getFile(
      @PathVariable fileId: UUID,
      request: HttpServletRequest,
      response: HttpServletResponse
  ) = download(fileId, response, request)
}
