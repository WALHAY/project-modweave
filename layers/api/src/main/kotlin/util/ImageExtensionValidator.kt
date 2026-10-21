package git.walhay.modweave.api.util

import git.walhay.modweave.domain.storage.ImageFormat
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.io.IOException
import java.util.Locale
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
        val format = ImageFormat.inspect(input, decode = true)
        extension in format.extensions
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
    val message: String =
        "Only valid PNG/JPEG images up to 4096 pixels per side and 16 megapixels are allowed",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
