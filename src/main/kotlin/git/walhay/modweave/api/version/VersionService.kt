package git.walhay.modweave.api.version

import git.walhay.modweave.api.file.FileService
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.exception.ModNotFoundException
import git.walhay.modweave.api.mod.repository.ModRepository
import git.walhay.modweave.api.version.dto.VersionUploadDto
import git.walhay.modweave.api.version.exception.VersionNotFoundException
import git.walhay.modweave.api.version.repository.VersionRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional
class VersionService(
    private val versionRepository: VersionRepository,
    private val fileService: FileService,
    private val modRepository: ModRepository,
) {

	fun uploadModVersion(mod: Mod, modVersionUploadDTO: VersionUploadDto): Version {
		val version = Version(modVersionUploadDTO.name, null, mod)
		mod.versions.addLast(version)

		fileService.uploadFilesTransient(version, modVersionUploadDTO.files)
		return versionRepository.save(version)
	}

	fun uploadModVersion(modId: String, modVersionUploadDTO: VersionUploadDto): Version {
		val mod =
			modRepository.findById(modId) ?: throw ModNotFoundException("Mod with id=$modId not found")
		return uploadModVersion(mod, modVersionUploadDTO)
	}

	fun uploadModVersionTransient(
        mod: Mod,
        name: String,
        changes: String?,
        files: List<MultipartFile>
	): Version {
		val version = Version(name, changes, mod)
		fileService.uploadFilesTransient(version, files)

		mod.versions.addLast(version)
		return version
	}

	fun uploadModVersionTransient(mod: Mod, name: String, files: List<MultipartFile>) =
		uploadModVersionTransient(mod, name, null, files)

	fun deleteModVersion(modId: String, version: String) {
		val mod =
			modRepository.findById(modId) ?: throw
				ModNotFoundException("Mod with modid=$modId not found")

		mod.versions.find { it.name == version }?.let { versionRepository.delete(it) }
			?: throw VersionNotFoundException("Version with name=$version not found for deletion")
	}
}
