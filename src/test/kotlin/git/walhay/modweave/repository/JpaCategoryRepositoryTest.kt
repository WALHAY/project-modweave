package git.walhay.modweave.api.category.repository

import git.walhay.modweave.api.category.Category
import git.walhay.modweave.api.category.CategoryId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@Tag("interaction")
class JpaCategoryRepositoryTest {
  private val springData = mock<SpringDataCategoryRepository>()
  private val repository = JpaCategoryRepository(springData)

  @Test
  fun `saves category through spring data`() {
    val category = Category(CategoryId("gameplay"), "Description")
    whenever(springData.save(any<CategoryEntity>()))
        .thenReturn(CategoryEntity.fromCategory(category))

    assertEquals(category, repository.save(category))
    verify(springData).save(any<CategoryEntity>())
  }

  @Test
  fun `maps all categories`() {
    val category = Category(CategoryId("gameplay"), "Description")
    whenever(springData.findAll()).thenReturn(listOf(CategoryEntity.fromCategory(category)))
    whenever(springData.findAllByNameIn(listOf("gameplay")))
        .thenReturn(setOf(CategoryEntity.fromCategory(category)))

    assertEquals(listOf(category), repository.findAll())
    assertTrue(repository.findAllByNameIn(listOf(category.name)).contains(category))
  }
}
