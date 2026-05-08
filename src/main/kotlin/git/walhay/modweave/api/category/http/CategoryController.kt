package git.walhay.modweave.api.category.http

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.category.ICategoryService
import git.walhay.modweave.api.category.http.dto.*
import jakarta.validation.Valid
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

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

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun uploadCategory(
      @Valid @ModelAttribute dto: CategoryUploadDto,
  ): CategoryResponseDto {
    logger.info { "POST /categories - uploading category: ${dto.name}" }
    return categoryService.uploadCategory(dto.toCategoryCreateCommand()).let {
      CategoryResponseDto.fromCategory(it)
    }
  }

  @DeleteMapping
  fun deleteCategory(
      @RequestParam categoryId: CategoryId,
  ): Unit {
    logger.info { "DELETE /categories - deleting category: $categoryId" }
    categoryService.deleteCategory(categoryId)
  }

  @PatchMapping
  fun updateCategory(
      @Valid @ModelAttribute dto: CategoryUpdateDto,
  ): CategoryResponseDto {
    logger.info { "PATCH /categories - updating category: ${dto.name}" }
    return categoryService.updateCategory(dto.toCategoryUpdateCommand()).let {
      CategoryResponseDto.fromCategory(it)
    }
  }
}
