package git.walhay.modweave.api.util

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.io.IOException
import java.util.Locale
import javax.imageio.ImageIO
import kotlin.reflect.KClass
import org.springframework.web.multipart.MultipartFile

class ImageExtensionValidator(
    private val allowedExtensions: Set<String> = setOf("png", "jpg", "jpeg"),
) : ConstraintValidator<ValidImage, MultipartFile?> {
  override fun isValid(value: MultipartFile?, context: ConstraintValidatorContext?): Boolean {
    if (value == null || value.isEmpty) return false
    val extension = value.originalFilename?.substringAfterLast('.', "")?.lowercase(Locale.ROOT)
    if (extension !in allowedExtensions) return false
    return try {
      value.inputStream.use { input ->
        ImageIO.createImageInputStream(input).use { image ->
          val readers = ImageIO.getImageReaders(image)
          if (!readers.hasNext()) return false
          val reader = readers.next()
          try {
            if (reader.formatName.lowercase(Locale.ROOT) !in setOf("png", "jpeg")) return false
            reader.input = image
            reader.read(0) != null
          } finally {
            reader.dispose()
          }
        }
      }
    } catch (_: IOException) {
      false
    } catch (_: IllegalArgumentException) {
      false
    }
  }
}

@MustBeDocumented
@Constraint(validatedBy = [ImageExtensionValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidImage(
    val message: String = "Only png, jpg or jpeg allowed",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
