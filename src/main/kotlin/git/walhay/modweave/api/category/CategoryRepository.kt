package git.walhay.modweave.api.category

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, String> {
  fun findAllByNameIn(categories: Collection<String>): Set<Category>
}
