package git.walhay.modweave.dto

import jakarta.validation.constraints.NotBlank
import org.springframework.web.multipart.MultipartFile

data class ModUploadForm (

    @NotBlank
    private val name: String,

    private val description: String,

    @NotBlank
    private val versionName: String,

    private val versionFiles: List<MultipartFile> = arrayListOf()
)