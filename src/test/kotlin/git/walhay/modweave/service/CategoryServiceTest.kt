package git.walhay.modweave.service

import git.walhay.modweave.api.category.*
import git.walhay.modweave.api.category.command.*
import git.walhay.modweave.api.category.exception.*
import git.walhay.modweave.api.category.repository.CategoryRepository
import git.walhay.modweave.testutils.TestFixtures
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class CategoryServiceTest : ServiceTestSupport() {
  private val repository = mock<CategoryRepository>()
  private val service = CategoryService(repository)

  @Test
  fun `gets categories from repository`() {
    val expected = listOf(TestFixtures.category())
    whenever(repository.findAll()).thenReturn(expected)

    val actual = service.getCategories()

    assertEquals(expected, actual)
    verify(repository).findAll()
  }

  @Test
  fun `rejects duplicate category`() {
    val command = CategoryCreateCommand(CategoryId("gameplay"), "Duplicate")
    whenever(repository.existsByNameIgnoreCase(command.name)).thenReturn(true)

    assertThrows(CategoryExistsException::class.java) { service.uploadCategory(command) }
    verify(repository, never()).save(any())
  }

  @Test
  fun `uploads category`() {
    val command = CategoryCreateCommand(CategoryId("performance"), "Performance")
    whenever(repository.existsByNameIgnoreCase(command.name)).thenReturn(false)
    whenever(repository.save(any())).thenAnswer { it.arguments[0] }

    val actual = service.uploadCategory(command)

    assertEquals(command.name, actual.name)
    verify(repository).save(any())
  }

  @Test
  fun `rejects update of missing category`() {
    val command = CategoryUpdateCommand(CategoryId("missing"), "Description")
    whenever(repository.existsByNameIgnoreCase(command.name)).thenReturn(false)

    assertThrows(CategoryNotFoundException::class.java) { service.updateCategory(command) }
    verify(repository, never()).save(any())
  }

  @Test
  fun `updates existing category`() {
    val command = CategoryUpdateCommand(CategoryId("gameplay"), "Updated")
    whenever(repository.existsByNameIgnoreCase(command.name)).thenReturn(true)
    whenever(repository.save(any())).thenAnswer { it.arguments[0] }

    assertEquals("Updated", service.updateCategory(command).description)
    verify(repository).save(any())
  }

  @Test
  fun `rejects deleting missing category`() {
    val id = CategoryId("missing")
    whenever(repository.existsByNameIgnoreCase(id)).thenReturn(false)

    assertThrows(CategoryNotFoundException::class.java) { service.deleteCategory(id) }
    verify(repository, never()).deleteByNameIgnoreCase(any())
  }

  @Test
  fun `deletes existing category`() {
    val id = CategoryId("gameplay")
    whenever(repository.existsByNameIgnoreCase(id)).thenReturn(true)

    service.deleteCategory(id)

    verify(repository).deleteByNameIgnoreCase(id)
  }
}
