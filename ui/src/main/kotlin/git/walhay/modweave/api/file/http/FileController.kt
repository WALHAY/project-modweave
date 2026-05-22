package git.walhay.modweave.api.file.http

import git.walhay.modweave.api.file.FileId
import git.walhay.modweave.api.file.repository.FileRepository
import git.walhay.modweave.api.storage.ISimpleStorageService
import jakarta.servlet.http.HttpServletResponse
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import mu.KLogger
import mu.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import org.springframework.util.StreamUtils

@RestController
@RequestMapping("/files")
class FileController(
    private val storageService: ISimpleStorageService,
    private val fileRepository: FileRepository,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @GetMapping("/{bucket}/{fileId}/download")
  fun downloadFile(
      @PathVariable bucket: String,
      @PathVariable fileId: Long,
      response: HttpServletResponse,
  ) {
    logger.info { "GET /files/$bucket/$fileId/download" }
    val file = fileRepository.findById(FileId(fileId))
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found")

    response.contentType = "application/octet-stream"
    response.setHeader(
        "Content-Disposition",
        "attachment; filename=\"${URLEncoder.encode(file.filename, StandardCharsets.UTF_8)}\"",
    )

    storageService.downloadVersionFile(file.filePath).use { input ->
      StreamUtils.copy(input, response.outputStream)
    }
  }
}
