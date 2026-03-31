package git.walhay.modweave.api.collection.repository

import git.walhay.modweave.api.collection.Collection
import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.mod.repository.ModEntity
import git.walhay.modweave.api.user.UserId
import io.mcarle.konvert.api.KonvertTo
import jakarta.persistence.*

@Entity
@Table(schema = "modweave", name = "collections")
@KonvertTo(Collection::class, mapFunctionName = "toModel")
class CollectionEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: CollectionId,
    @Column("name", nullable = false) val name: String,
    @Column("description") val description: String?,
    @Column("owner", nullable = false) val owner: UserId,
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        schema = "modweave",
        name = "collections_mods",
        joinColumns = [JoinColumn("collection_id")],
        inverseJoinColumns = [JoinColumn("mod_id")])
    val mods: List<ModEntity> = emptyList()
) {
  constructor() : this(CollectionId(), "", null, UserId())
}
