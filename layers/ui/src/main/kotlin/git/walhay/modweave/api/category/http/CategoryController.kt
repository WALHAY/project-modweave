package git.walhay.modweave.api.category.http

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.category.ICategoryService
import git.walhay.modweave.api.category.exception.CategoryNotFoundException
import git.walhay.modweave.api.category.http.dto.*
import jakarta.validation.Valid
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/categories")
class CategoryController(
    val categoryService: ICategoryService,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @GetMapping
  fun getCategories(): List<CategoryResponseDto> {
    logger.debug { "GET /categories" }
    return categoryService.getCategories().map { CategoryResponseDto.fromCategory(it) }
  }

  @PostMapping(version = "1")
  @ResponseStatus(HttpStatus.CREATED)
  fun uploadCategory(
      @Valid @ModelAttribute dto: CategoryUploadDto,
  ): CategoryResponseDto {
    logger.info { "POST /categories - uploading category: ${dto.name}" }
    return categoryService.uploadCategory(dto.toCategoryCreateCommand()).let {
      CategoryResponseDto.fromCategory(it)
    }
  }

  @DeleteMapping(version = "1")
  fun deleteCategory(
      @RequestParam category: String,
  ): Unit {
    logger.info { "DELETE /categories - deleting category: $category" }
    categoryService.deleteCategory(CategoryId(category))
  }

  @PatchMapping(version = "1")
  fun updateCategory(
      @Valid @ModelAttribute dto: CategoryUpdateDto,
  ): CategoryResponseDto {
    logger.info { "PATCH /categories - updating category: ${dto.name}" }
    return categoryService.updateCategory(dto.toCategoryUpdateCommand()).let {
      CategoryResponseDto.fromCategory(it)
    }
  }

  @GetMapping("/{categoryName}", version = "2")
  fun getCategory(@PathVariable categoryName: String): CategoryResponseDto =
      categoryService
          .getCategories()
          .firstOrNull { it.name.value.equals(categoryName, ignoreCase = true) }
          ?.let { CategoryResponseDto.fromCategory(it) }
          ?: throw CategoryNotFoundException(CategoryId(categoryName))

  @PostMapping(version = "2")
  fun createCategory(@RequestBody body: Map<String, Any?>): ResponseEntity<CategoryResponseDto> {
    require(body.keys.all { it in setOf("name", "description") }) { "Unknown category field" }
    val name = body["name"]
    require(name is String && name.isNotBlank() && name.length <= 50) {
      "Category name must contain 1 to 50 characters"
    }
    val description = description(body)
    val result = uploadCategory(CategoryUploadDto(name, description))
    val location =
        UriComponentsBuilder.fromPath("/api/v2/categories/{name}")
            .buildAndExpand(result.name)
            .encode()
            .toUri()
    return ResponseEntity.created(location).body(result)
  }

  @PatchMapping("/{categoryName}", version = "2")
  fun patchCategory(
      @PathVariable categoryName: String,
      @RequestBody body: Map<String, Any?>,
  ): CategoryResponseDto {
    require(body.keys == setOf("description")) { "Exactly the description field is required" }
    return updateCategory(CategoryUpdateDto(categoryName, description(body)))
  }

  @DeleteMapping("/{categoryName}", version = "2")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun removeCategory(@PathVariable categoryName: String) = deleteCategory(categoryName)

  private fun description(body: Map<String, Any?>): String? {
    val value = body["description"]
    require(value == null || value is String) { "Description must be a string or null" }
    return value
  }
}
