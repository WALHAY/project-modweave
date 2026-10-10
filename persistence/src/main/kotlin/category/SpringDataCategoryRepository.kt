package git.walhay.modweave.persistence.category

import org.springframework.data.jpa.repository.JpaRepository

interface SpringDataCategoryRepository : JpaRepository<CategoryEntity, String> {
  fun findAllByNameIn(categories: Collection<String>): Set<CategoryEntity>

  fun existsByNameIgnoreCase(name: String): Boolean

  fun deleteByNameIgnoreCase(name: String)
}
