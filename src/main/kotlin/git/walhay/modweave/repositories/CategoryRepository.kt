package git.walhay.modweave.repositories

import git.walhay.modweave.models.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, String> {
  fun findAllByNameIn(categories: Collection<String>): Set<Category>
}
