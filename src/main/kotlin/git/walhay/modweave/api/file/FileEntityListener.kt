package git.walhay.modweave.api.file

import git.walhay.modweave.api.file.repository.FileEntity
import git.walhay.modweave.api.service.SimpleStorageService
import jakarta.persistence.PreRemove
import mu.KLogger
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class FileEntityListener(
	private val simpleStorageService: SimpleStorageService,
	private val logger: KLogger = KotlinLogging.logger {}
) {
	@PreRemove
	fun onFileRemoval(file: FileEntity) {
		logger.info { "Remove file with name `${file.filePath}`" }
		simpleStorageService.removeVersionFile(file.filePath)
	}
}
