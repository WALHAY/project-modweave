package git.walhay.modweave.api.collection.repository

import git.walhay.modweave.api.collection.Collection
import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.mod.repository.ModEntity
import git.walhay.modweave.api.user.UserId
import jakarta.persistence.*
import java.io.Serializable
import java.util.UUID

@Entity
@Table(schema = "modweave", name = "collections")
class CollectionEntity(
    @Id @Column("id") val id: UUID,
    @Column("name", nullable = false) val name: String,
    @Column("description") val description: String?,
    @Column("owner", nullable = false) val owner: String,
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", insertable = false, updatable = false)
    @OrderBy("index")
    val mods: List<CollectionItemEntity> = emptyList(),
) : Serializable {
  constructor() : this(UUID.randomUUID(), "", null, "")

  fun toDomain(): Collection =
      Collection(
          CollectionId(id),
          name,
          description,
          UserId(owner),
          mods.sortedBy { it.index }.map { it.mod.toDomain() }.toMutableList(),
      )

  companion object {
    fun fromCollection(collection: Collection): CollectionEntity =
        collection.let { (id, name, description, owner, mods) ->
          CollectionEntity(
              id.value,
              name,
              description,
              owner.value,
              mods.mapIndexed { index, mod ->
                CollectionItemEntity(index, id.value, ModEntity.fromMod(mod))
              },
          )
        }
  }
}
