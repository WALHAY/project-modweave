package git.walhay.modweave.cli

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.category.ICategoryService
import git.walhay.modweave.api.category.command.CategoryCreateCommand
import git.walhay.modweave.api.category.command.CategoryUpdateCommand
import git.walhay.modweave.api.category.http.dto.CategoryResponseDto
import org.springframework.shell.core.command.annotation.Command
import org.springframework.shell.core.command.annotation.Option
import org.springframework.stereotype.Component

@Component
class CategoryShellCommands(
    private val categoryService: ICategoryService,
) : ShellCommandSupport() {
  @Command(name = ["category", "list"], description = "List all category.")
  fun categoryList(): Any =
      renderValue(categoryService.getCategories().map { CategoryResponseDto.fromCategory(it) })

  @Command(name = ["category", "create"], description = "Create a category.")
  fun categoryCreate(
      @Option(longName = "name") name: String,
      @Option(longName = "description", required = false) description: String?,
  ): Any =
      renderValue(
          CategoryResponseDto.fromCategory(
              categoryService.uploadCategory(CategoryCreateCommand(CategoryId(name), description)),
          ),
      )

  @Command(name = ["category", "update"], description = "Update a category.")
  fun categoryUpdate(
      @Option(longName = "name") name: String,
      @Option(longName = "description", required = false) description: String?,
  ): Any =
      renderValue(
          CategoryResponseDto.fromCategory(
              categoryService.updateCategory(CategoryUpdateCommand(CategoryId(name), description)),
          ),
      )

  @Command(name = ["category", "delete"], description = "Delete a category.")
  fun categoryDelete(
      @Option(longName = "name") name: String,
  ): String {
    categoryService.deleteCategory(CategoryId(name))
    return "Category '$name' deleted."
  }
}
