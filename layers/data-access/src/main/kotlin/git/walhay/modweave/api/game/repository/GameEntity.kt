package git.walhay.modweave.api.game.repository

import git.walhay.modweave.api.game.Game
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.mod.repository.ModEntity
import git.walhay.modweave.util.spinalCase
import jakarta.persistence.*

@Entity
@Table(schema = "modweave", name = "games")
class GameEntity(
    @Id @Column(name = "id", nullable = false) val id: String,
    @Column(name = "name", nullable = false) val name: String,
    @Column(name = "description", columnDefinition = "text") val description: String? = null,
    @Column(name = "image_path", nullable = false) val imagePath: String,
    @OneToMany(mappedBy = "gameId", fetch = FetchType.LAZY)
    val mods: MutableList<ModEntity> = mutableListOf(),
) {
  constructor() : this("", "", null, "")

  constructor(
      name: String,
      description: String? = null,
      imagePath: String,
  ) : this(id = name.spinalCase(), name = name, description = description, imagePath = imagePath)

  fun toDomain(): Game =
      Game(GameId(id), name, description, imagePath, mods.map { it.toDomain() }.toMutableList())

  companion object {
    fun fromGame(game: Game): GameEntity =
        GameEntity(
            game.id.value,
            game.name,
            game.description,
            game.imagePath,
            game.mods.map { ModEntity.fromMod(it) }.toMutableList())
  }
}
