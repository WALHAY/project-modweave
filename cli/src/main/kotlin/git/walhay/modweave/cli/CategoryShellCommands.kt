package git.walhay.modweave.cli

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.category.ICategoryService
import git.walhay.modweave.api.category.command.CategoryCreateCommand
import git.walhay.modweave.api.category.command.CategoryUpdateCommand
import org.springframework.shell.core.command.annotation.Command
import org.springframework.shell.core.command.annotation.Option
import org.springframework.stereotype.Component

@Component
class CategoryShellCommands(
    private val categoryService: ICategoryService,
) : ShellCommandSupport() {
  @Command(name = ["categories", "list"], description = "List all categories.")
  fun categoriesList(): Any = renderValue(categoryService.getCategories())

  @Command(name = ["categories", "create"], description = "Create a category.")
  fun categoriesCreate(
      @Option(longName = "name") name: String,
      @Option(longName = "description", required = false) description: String?,
  ): Any =
      renderValue(categoryService.uploadCategory(CategoryCreateCommand(CategoryId(name), description)))

  @Command(name = ["categories", "update"], description = "Update a category.")
  fun categoriesUpdate(
      @Option(longName = "name") name: String,
      @Option(longName = "description", required = false) description: String?,
  ): Any =
      renderValue(categoryService.updateCategory(CategoryUpdateCommand(CategoryId(name), description)))

  @Command(name = ["categories", "delete"], description = "Delete a category.")
  fun categoriesDelete(
      @Option(longName = "name") name: String,
  ): String {
    categoryService.deleteCategory(CategoryId(name))
    return "Category '$name' deleted."
  }
}
