package git.walhay.modweave.api.category

import git.walhay.modweave.api.category.dto.CategoryResponseDto
import git.walhay.modweave.api.category.dto.CategoryUpdateDto
import git.walhay.modweave.api.category.dto.CategoryUploadDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories")
class CategoryController(val categoryService: ICategoryService) {

  @GetMapping
  fun getCategories(): List<CategoryResponseDto> =
      categoryService.getCategories().map { it.toCategoryResponseDto() }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun uploadCategory(@Valid @ModelAttribute dto: CategoryUploadDto): CategoryResponseDto =
      categoryService.uploadCategory(dto).toCategoryResponseDto()

  @DeleteMapping
  fun deleteCategory(@RequestParam categoryId: CategoryId): Unit =
      categoryService.deleteCategory(categoryId)

  @PatchMapping
  fun updateCategory(@Valid @ModelAttribute dto: CategoryUpdateDto): CategoryResponseDto =
      categoryService.updateCategory(dto).toCategoryResponseDto()
}
