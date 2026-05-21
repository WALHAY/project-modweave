package git.walhay.modweave.util

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.apache.commons.io.FilenameUtils
import org.springframework.web.multipart.MultipartFile

class ImageExtensionValidator(
    private val allowedExtensions: Set<String> = setOf("png", "jpg", "jpeg"),
) : ConstraintValidator<ValidImage, MultipartFile?> {
  override fun isValid(
      value: MultipartFile?,
      context: ConstraintValidatorContext?,
  ): Boolean = FilenameUtils.getExtension(value?.originalFilename).lowercase() in allowedExtensions
}
