package git.walhay.modweave.util

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [ImageExtensionValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidImage(
    val message: String = "Only png, jpg or jpeg allowed",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
