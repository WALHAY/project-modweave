package git.walhay.modweave.api.version.http.dto

import git.walhay.modweave.api.version.command.VersionCreateCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.springframework.web.multipart.MultipartFile

data class VersionUploadDto(
    @field:NotBlank val name: String,
    val changes: String?,
    @field:NotEmpty val files: List<MultipartFile>,
) {
  fun toVersionCreateCommand(): VersionCreateCommand = VersionCreateCommand(name, changes, files)
}
