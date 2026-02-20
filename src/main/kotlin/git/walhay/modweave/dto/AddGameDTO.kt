package git.walhay.modweave.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.web.multipart.MultipartFile

data class AddGameDTO(@NotBlank val name: String, val description: String, @NotNull val image: MultipartFile)
