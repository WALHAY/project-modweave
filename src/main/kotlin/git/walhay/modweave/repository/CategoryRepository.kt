package git.walhay.modweave.repository

import git.walhay.modweave.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, String> {
  fun findAllByNameIn(categories: Collection<String>): Set<Category>
}
