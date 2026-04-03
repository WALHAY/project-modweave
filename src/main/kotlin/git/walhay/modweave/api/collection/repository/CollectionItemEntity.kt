package git.walhay.modweave.api.collection.repository

import git.walhay.modweave.api.collection.CollectionId
import git.walhay.modweave.api.mod.repository.ModEntity
import jakarta.persistence.*

@Entity
@Table(schema = "modweave", name = "collections_mods")
@IdClass(CollectionItemId::class)
class CollectionItemEntity(
    @Id @Column("\"index\"") val index: Int,
    @Id @Column("collection_id", nullable = false) val collectionId: CollectionId,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn("mod_id", nullable = false) val mod: ModEntity,
) {
    constructor() : this(0, CollectionId(), ModEntity())
}