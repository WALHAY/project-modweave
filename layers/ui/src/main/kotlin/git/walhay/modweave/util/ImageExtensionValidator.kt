package git.walhay.modweave.util

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import javax.imageio.ImageIO
import org.apache.commons.io.FilenameUtils
import org.springframework.web.multipart.MultipartFile

class ImageExtensionValidator(
    private val allowedExtensions: Set<String> = setOf("png", "jpg", "jpeg"),
) : ConstraintValidator<ValidImage, MultipartFile?> {
  override fun isValid(
      value: MultipartFile?,
      context: ConstraintValidatorContext?,
  ): Boolean {
    if (value == null || value.isEmpty) return false
    val extension = FilenameUtils.getExtension(value.originalFilename ?: return false).lowercase()
    if (extension !in allowedExtensions) return false
    return runCatching {
          value.inputStream.use { input ->
            ImageIO.createImageInputStream(input).use { stream ->
              val readers = ImageIO.getImageReaders(stream)
              if (!readers.hasNext()) return false
              val reader = readers.next()
              try {
                reader.input = stream
                val format = reader.formatName.lowercase()
                val matches =
                    if (extension == "png") format == "png" else format in setOf("jpeg", "jpg")
                matches && reader.getWidth(0) > 0 && reader.getHeight(0) > 0
              } finally {
                reader.dispose()
              }
            }
          }
        }
        .getOrDefault(false)
  }
}
