package git.walhay.modweave.api.mod.repository

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.repository.VersionEntity
import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime

@Entity
@Table(schema = "modweave", name = "mods")
class ModEntity(
    @Id @Column(name = "id", nullable = false, length = 50) val id: String,
    @Column(name = "name", nullable = false, length = 255) val name: String,
    @Column(name = "description", columnDefinition = "text") val description: String? = null,
    @Column(name = "image_path", nullable = false, length = 500) val imagePath: String,
    @Column(name = "creation_date", nullable = false) val creationDate: LocalDateTime,
    @Column(name = "publisher_id", nullable = false) val publisherId: String,
    @Column(name = "game_id", nullable = false) val gameId: String,
    @ElementCollection
    @CollectionTable(
        schema = "modweave",
        name = "mods_categories",
        joinColumns = [JoinColumn(name = "mod_id", nullable = false)],
    )
    @Column(name = "category_name")
    val categories: Set<String> = emptySet(),
    @OneToMany(
        mappedBy = "modId",
        fetch = FetchType.LAZY,
        orphanRemoval = true,
        cascade = [CascadeType.ALL],
    )
    val versions: MutableList<VersionEntity> = mutableListOf(),
) : Serializable {
  constructor() : this("", "", null, "", LocalDateTime.now(), "", "")

  fun toDomain(): Mod =
      Mod(
          ModId(id),
          name,
          description,
          imagePath,
          creationDate,
          UserId(publisherId),
          GameId(gameId),
          categories.map { CategoryId(it) }.toSet(),
          versions.map { it.toDomain() }.toMutableList())

  companion object {
    fun fromMod(mod: Mod): ModEntity =
        ModEntity(
            mod.id.value,
            mod.name,
            mod.description,
            mod.imagePath,
            mod.creationDate,
            mod.publisherId.value,
            mod.gameId.value,
            mod.categories.map { it.value }.toSet(),
            mod.versions.map { VersionEntity.fromVersion(it) }.toMutableList())
  }
}
