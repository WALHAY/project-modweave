package git.walhay.modweave.api.collection.repository

import git.walhay.modweave.api.mod.repository.ModEntity
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(schema = "modweave", name = "collections_mods")
@IdClass(CollectionItemId::class)
class CollectionItemEntity(
    @Id @Column("order_index") val index: Int,
    @Id @Column("collection_id", nullable = false) val collectionId: UUID,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn("mod_id", nullable = false) val mod: ModEntity,
) {
  constructor() : this(0, UUID.randomUUID(), ModEntity())
}
