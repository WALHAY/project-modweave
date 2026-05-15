package git.walhay.modweave.api.collection.repository

import git.walhay.modweave.api.collection.Collection
import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.mod.repository.ModEntity
import git.walhay.modweave.api.mod.repository.fromMod
import git.walhay.modweave.api.mod.repository.toDomain
import git.walhay.modweave.api.user.UserId
import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(schema = "modweave", name = "collections")
class CollectionEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column("id") val id: Long,
    @Column("name", nullable = false) val name: String,
    @Column("description") val description: String?,
    @Column("owner", nullable = false) val owner: UserId,
    @OneToMany(mappedBy = "collectionId", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @OrderBy("order_index")
    val mods: List<CollectionItemEntity> = emptyList(),
) : Serializable {
  constructor() : this(0, "", null, UserId())

  fun toDomain(): Collection =
      Collection(
          CollectionId(id),
          name,
          description,
          owner,
          mods.sortedBy { it.index }.map { it.mod.toDomain() }.toMutableList(),
      )

  companion object {
    fun fromCollection(collection: Collection): CollectionEntity =
        collection.let { (id, name, description, owner, mods) ->
          CollectionEntity(
              id.value,
              name,
              description,
              owner,
              mods.mapIndexed { index, mod ->
                CollectionItemEntity(index, id, ModEntity.fromMod(mod))
              },
          )
        }
  }
}
