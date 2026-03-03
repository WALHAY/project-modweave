package git.walhay.modweave.api.file

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
	private val fileRepository: FileRepository
) {

	@GetMapping("/{bucket}/{fileId}/download")
	fun downloadFile(
		@PathVariable bucket: String,
		@PathVariable fileId: Long,
		response: HttpServletResponse
	) {
		val file = fileRepository.findById(fileId).orElseThrow()
		val filename = file.filePath
		file.downloads++
		fileRepository.save(file)

		response.sendRedirect("${minioUrl}/$bucket/$filename")
	}
}
