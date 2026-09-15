package git.walhay.modweave.service

import git.walhay.modweave.api.category.Category
import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.category.CategoryService
import git.walhay.modweave.api.category.command.CategoryCreateCommand
import git.walhay.modweave.api.category.repository.CategoryRepository
import git.walhay.modweave.testutils.InteractionTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@InteractionTest
class StubStyleServiceTest {
  @Test
  fun `category service creates category using repository stub`() {
    val repository = InMemoryCategoryRepository()
    val service = CategoryService(repository)
    val command = CategoryCreateCommand(CategoryId("performance"), "Performance mods")

    val created = service.uploadCategory(command)

    assertEquals(command.name, created.name)
    assertEquals(command.description, created.description)
    assertEquals(listOf(created), repository.savedCategories)
  }

  @Test
  fun `category service returns data supplied by repository stub`() {
    val expected =
        listOf(
            Category(CategoryId("gameplay"), "Gameplay mods"),
            Category(CategoryId("optimization"), "Optimization mods"))
    val repository = InMemoryCategoryRepository(expected)
    val service = CategoryService(repository)

    val actual = service.getCategories()

    assertEquals(expected, actual)
  }

  private class InMemoryCategoryRepository(initial: List<Category> = emptyList()) :
      CategoryRepository {
    private val categories = initial.toMutableList()
    val savedCategories = mutableListOf<Category>()

    override fun findAll(): List<Category> = categories.toList()

    override fun findAllByNameIn(categories: Collection<CategoryId>): Set<Category> =
        this.categories.filter { it.name in categories }.toSet()

    override fun existsByNameIgnoreCase(categoryId: CategoryId): Boolean =
        categories.any { it.name.value.equals(categoryId.value, ignoreCase = true) }

    override fun deleteByNameIgnoreCase(categoryId: CategoryId) {
      categories.removeIf { it.name.value.equals(categoryId.value, ignoreCase = true) }
    }

    override fun save(category: Category): Category {
      categories.removeIf { it.name == category.name }
      categories.add(category)
      savedCategories.add(category)
      return category
    }
  }
}
