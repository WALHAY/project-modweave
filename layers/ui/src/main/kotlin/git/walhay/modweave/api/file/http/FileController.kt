package git.walhay.modweave.api.file.http

import git.walhay.modweave.api.file.FileId
import git.walhay.modweave.api.file.repository.FileRepository
import git.walhay.modweave.api.storage.ISimpleStorageService
import git.walhay.modweave.api.version.IVersionService
import jakarta.servlet.http.HttpServletResponse
import java.nio.charset.StandardCharsets
import java.util.UUID
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpStatus
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/files")
class FileController(
    private val storageService: ISimpleStorageService,
    private val fileRepository: FileRepository,
    private val versionService: IVersionService,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @GetMapping(value = ["/{fileId}/download", "/mods/{fileId}/download"], version = "1")
  fun downloadFile(
      @PathVariable fileId: UUID,
      response: HttpServletResponse,
  ) {
    logger.info { "GET /files/$fileId/download" }
    val file =
        fileRepository.findById(FileId(fileId))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found")
    versionService.getModVersion(file.versionId)

    response.contentType = "application/octet-stream"
    response.setHeader(
        "Content-Disposition",
        ContentDisposition.attachment()
            .filename(file.filename, StandardCharsets.UTF_8)
            .build()
            .toString(),
    )

    storageService.downloadVersionFile(file.filePath).use { input ->
      StreamUtils.copy(input, response.outputStream)
    }
    fileRepository.incrementDownloads(file.id)
  }

  @GetMapping("/{fileId}", version = "2")
  fun getFile(@PathVariable fileId: UUID, response: HttpServletResponse) =
      downloadFile(fileId, response)
}
