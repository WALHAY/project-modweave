package git.walhay.modweave.regression

import git.walhay.modweave.persistence.category.CategoryEntity
import git.walhay.modweave.persistence.collection.CollectionEntity
import git.walhay.modweave.persistence.collection.CollectionItemEntity
import git.walhay.modweave.persistence.comment.CommentEntity
import git.walhay.modweave.persistence.file.FileEntity
import git.walhay.modweave.persistence.game.GameEntity
import git.walhay.modweave.persistence.mod.ModEntity
import git.walhay.modweave.persistence.user.UserEntity
import git.walhay.modweave.persistence.version.VersionEntity
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.junit.jupiter.api.Test

class HibernateMappingTest {
  @Test
  fun `all entity mappings can build a PostgreSQL session factory`() {
    val registry =
        StandardServiceRegistryBuilder()
            .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
            .applySetting("hibernate.boot.allow_jdbc_metadata_access", false)
            .applySetting("hibernate.hbm2ddl.auto", "none")
            .build()
    try {
      val sources = MetadataSources(registry)
      listOf(
              CategoryEntity::class.java,
              CollectionEntity::class.java,
              CollectionItemEntity::class.java,
              CommentEntity::class.java,
              FileEntity::class.java,
              GameEntity::class.java,
              ModEntity::class.java,
              UserEntity::class.java,
              VersionEntity::class.java)
          .forEach { sources.addAnnotatedClass(it) }
      sources.buildMetadata().buildSessionFactory().close()
    } finally {
      StandardServiceRegistryBuilder.destroy(registry)
    }
  }
}
