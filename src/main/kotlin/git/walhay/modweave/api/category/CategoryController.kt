package git.walhay.modweave.api.category

import git.walhay.modweave.api.category.dto.CategoryDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categories")
class CategoryController(val categoryService: CategoryService) {

  @GetMapping fun getCategories(): List<CategoryDto> = categoryService.getCategories()

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun uploadCategory(@Valid @ModelAttribute dto: CategoryDto): CategoryDto =
      categoryService.uploadCategory(dto)

  @DeleteMapping
  fun deleteCategory(@RequestParam category: String) = categoryService.deleteCategory(category)

  @PatchMapping
  fun updateCategory(@Valid @ModelAttribute dto: CategoryDto): CategoryDto =
      categoryService.updateCategory(dto)
}
