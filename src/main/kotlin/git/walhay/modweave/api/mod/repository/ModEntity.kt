package git.walhay.modweave.api.mod.repository

import git.walhay.modweave.api.category.CategoryId
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.repository.VersionEntity
import io.mcarle.konvert.api.KonvertFrom
import io.mcarle.konvert.api.KonvertTo
import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(schema = "modweave", name = "mods")
@KonvertTo(Mod::class, mapFunctionName = "toDomain")
@KonvertFrom(Mod::class)
class ModEntity(
    @Id @Column(name = "id", nullable = false, length = 50) val id: String,
    @Column(name = "name", nullable = false, length = 255) val name: String,
    @Column(name = "description", columnDefinition = "text") val description: String? = null,
    @Column(name = "image_path", nullable = false, length = 500) val imagePath: String,
    @Column(name = "creation_date", nullable = false) val creationDate: LocalDateTime,
    @Column(name = "publisher_id", nullable = false) val publisherId: UserId,
    @Column(name = "game_id", nullable = false) val gameId: GameId,
    @ElementCollection
    @CollectionTable(
        schema = "modweave",
        name = "mods_categories",
        joinColumns = [JoinColumn(name = "mod_id", nullable = false)],
    )
    @Column(name = "category_name")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    val categories: List<CategoryId> = emptyList(),
    @OneToMany(
        mappedBy = "modId",
        fetch = FetchType.LAZY,
        orphanRemoval = true,
        cascade = [CascadeType.ALL],
    )
    val versions: MutableList<VersionEntity> = mutableListOf(),
) : Serializable {
  constructor() : this("", "", null, "", LocalDateTime.now(), UserId(), GameId())

  companion object
}
