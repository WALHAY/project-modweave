package git.walhay.modweave.api.category

import git.walhay.modweave.api.common.apiLink
import git.walhay.modweave.domain.category.CategoryId
import git.walhay.modweave.domain.category.CategoryNotFoundException
import git.walhay.modweave.domain.category.CategoryService
import jakarta.validation.Valid
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories")
class CategoryController(
    val categoryService: CategoryService,
) {
  private val logger: KLogger = KotlinLogging.logger {}

  @GetMapping(version = "1")
  fun getCategories(): List<CategoryResponseDto> {
    logger.debug { "GET /categories" }
    return categoryService.getCategories().map { CategoryResponseDto.fromCategory(it) }
  }

  @GetMapping(version = "2")
  fun listCategories(): CategoryCollectionDto<CategoryResponseDto> =
      CategoryCollectionDto(getCategories())

  @PostMapping(
      version = "1", consumes = ["application/x-www-form-urlencoded", "multipart/form-data"])
  @ResponseStatus(HttpStatus.CREATED)
  fun uploadCategory(
      @Valid @ModelAttribute dto: CategoryUploadDto,
  ): CategoryResponseDto {
    logger.info { "POST /categories - uploading category: ${dto.name}" }
    return categoryService.uploadCategory(dto.toCategoryCommand()).let {
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

  @PatchMapping(
      version = "1", consumes = ["application/x-www-form-urlencoded", "multipart/form-data"])
  fun updateCategory(
      @Valid @ModelAttribute dto: CategoryUpdateDto,
  ): CategoryResponseDto {
    logger.info { "PATCH /categories - updating category: ${dto.name}" }
    return categoryService.updateCategory(dto.toCategoryCommand()).let {
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

  @PostMapping(version = "2", consumes = ["application/json"])
  fun createCategory(
      @Valid @RequestBody dto: CategoryUploadDto
  ): ResponseEntity<CategoryResponseDto> {
    val result = uploadCategory(dto)
    return ResponseEntity.created(
            apiLink(CategoryController::getCategory, result.name).link.toUri())
        .body(result)
  }

  @PatchMapping("/{categoryName}", version = "2", consumes = ["application/json"])
  fun patchCategory(
      @PathVariable categoryName: String,
      @RequestBody dto: CategoryPatchDto,
  ): CategoryResponseDto {
    return updateCategory(CategoryUpdateDto(categoryName, dto.description))
  }

  @DeleteMapping("/{categoryName}", version = "2")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun removeCategory(@PathVariable categoryName: String) = deleteCategory(categoryName)
}
