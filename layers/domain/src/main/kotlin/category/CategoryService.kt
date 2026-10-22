package git.walhay.modweave.domain.category

import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CategoryService(private val categoryRepository: CategoryRepository) {
  @Cacheable("categories") fun getCategories(): List<Category> = categoryRepository.findAll()

  @CacheEvict(value = ["categories", "mods", "games", "users", "collections"], allEntries = true)
  @PreAuthorize("@accessSecurity.isAdmin()")
  fun uploadCategory(command: CategoryCommand): Category {
    if (categoryRepository.existsByNameIgnoreCase(command.name))
        throw CategoryExistsException(command.name)
    return categoryRepository.save(Category(command.name, command.description))
  }

  @CacheEvict(value = ["categories", "mods", "games", "users", "collections"], allEntries = true)
  @PreAuthorize("@accessSecurity.isAdmin()")
  fun updateCategory(command: CategoryCommand): Category {
    val existing =
        categoryRepository.findAll().firstOrNull {
          it.name.value.equals(command.name.value.trim(), ignoreCase = true)
        } ?: throw CategoryNotFoundException(command.name)
    return categoryRepository.save(Category(existing.name, command.description))
  }

  @CacheEvict(value = ["categories", "mods", "games", "users", "collections"], allEntries = true)
  @PreAuthorize("@accessSecurity.isAdmin()")
  fun deleteCategory(categoryId: CategoryId) {
    if (!categoryRepository.existsByNameIgnoreCase(categoryId))
        throw CategoryNotFoundException(categoryId)
    categoryRepository.deleteByNameIgnoreCase(categoryId)
  }
}
