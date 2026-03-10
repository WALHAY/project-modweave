package git.walhay.modweave.api.file

import git.walhay.modweave.api.service.SimpleStorageService
import git.walhay.modweave.api.version.Version
import git.walhay.modweave.api.version.toEntity
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional
class FileService(
	private val simpleStorageService: SimpleStorageService
) {

	fun incrementDownloadCounter() {}

	fun uploadFilesTransient(version: Version, files: List<MultipartFile>) {
		for (file in files) {
			val filename = "${version.mod.name}/${version.name}/${file.originalFilename}"
			simpleStorageService.uploadVersionFile(filename, file)

			version.files.addFirst(File(file.name, filename, version.toEntity()))
		}
	}
}
