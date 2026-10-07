package git.walhay.modweave.util

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import javax.imageio.ImageIO
import org.springframework.web.multipart.MultipartFile

class ImageExtensionValidator(
    private val allowedExtensions: Set<String> = setOf("png", "jpg", "jpeg"),
) : ConstraintValidator<ValidImage, MultipartFile?> {
  override fun isValid(value: MultipartFile?, context: ConstraintValidatorContext?) =
      value?.takeIf { !it.isEmpty }?.inputStream?.use { ImageIO.read(it) != null } == true
}
