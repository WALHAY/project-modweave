package git.walhay.modweave.repository

import git.walhay.modweave.api.category.Category
import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.category.repository.CategoryRepository
import git.walhay.modweave.api.category.repository.JpaCategoryRepository
import git.walhay.modweave.testutils.PostgresTestTemplate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

@Import(JpaCategoryRepository::class)
class CategoryRepositoryTest : PostgresTestTemplate() {

  @Autowired lateinit var categoryRepository: CategoryRepository

  private fun seedCategory(name: String = "Gameplay", description: String? = "desc"): Category {
    return categoryRepository.save(Category(CategoryId(name), description))
  }

  @Test
  fun `create category`() {
    val created = seedCategory()
    assertNotNull(created)
    assertEquals("Gameplay", created.name.value)
    assertTrue(categoryRepository.existsByNameIgnoreCase(CategoryId("gameplay")))
  }

  @Test
  fun `read category`() {
    val created = seedCategory()

    val all = categoryRepository.findAll()
    assertTrue(all.any { it.name.value.equals(created.name.value, ignoreCase = true) })

    val picked =
        categoryRepository.findAllByNameIn(listOf(CategoryId("Gameplay"), CategoryId("Other")))
    assertTrue(picked.any { it.name.value.equals("Gameplay", ignoreCase = true) })
  }

  @Test
  fun `update category`() {
    val created = seedCategory(description = "old")

    val updated = categoryRepository.save(created.copy(description = "new"))
    assertEquals(created.name.value, updated.name.value)

    val refetched = categoryRepository.findAllByNameIn(listOf(created.name)).firstOrNull()
    assertNotNull(refetched)
    assertEquals("new", refetched!!.description)
  }

  @Test
  fun `delete category`() {
    val created = seedCategory()

    categoryRepository.deleteByNameIgnoreCase(CategoryId(created.name.value.lowercase()))

    assertFalse(categoryRepository.existsByNameIgnoreCase(created.name))

    val after = categoryRepository.findAllByNameIn(listOf(created.name))
    assertTrue(after.isEmpty())
  }
}
