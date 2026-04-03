package git.walhay.modweave.api.collection.repository

import git.walhay.modweave.api.collection.Collection
import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.mod.Mod
import git.walhay.modweave.api.mod.repository.toModel
import git.walhay.modweave.api.user.UserId
import io.mcarle.konvert.api.KonvertTo
import io.mcarle.konvert.api.Mapping
import jakarta.persistence.*

@Entity
@Table(schema = "modweave", name = "collections")
@KonvertTo(Collection::class, mapFunctionName = "toModel", mappings = [
    Mapping("mods", expression = "modsListToMap()")
])
class CollectionEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: CollectionId,
    @Column("name", nullable = false) val name: String,
    @Column("description") val description: String?,
    @Column("owner", nullable = false) val owner: UserId,
    @OneToMany(mappedBy = "collectionId", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val mods: List<CollectionItemEntity> = emptyList()
) {
  constructor() : this(CollectionId(), "", null, UserId())

    fun modsListToMap(): MutableMap<Int, Mod> = this.mods.associate { it.index to it.mod.toModel() }.toMutableMap()
}
