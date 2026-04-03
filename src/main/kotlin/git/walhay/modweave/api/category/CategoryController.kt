package git.walhay.modweave.api.category

import git.walhay.modweave.api.category.dto.CategoryResponseDto
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
  fun uploadCategory(@Valid @ModelAttribute dto: CategoryResponseDto): CategoryResponseDto =
      categoryService.uploadCategory(dto).toCategoryResponseDto()

  @DeleteMapping
  fun deleteCategory(@RequestParam category: String): Unit =
      categoryService.deleteCategory(category)

  @PatchMapping
  fun updateCategory(@Valid @ModelAttribute dto: CategoryResponseDto): CategoryResponseDto =
      categoryService.updateCategory(dto).toCategoryResponseDto()
}
