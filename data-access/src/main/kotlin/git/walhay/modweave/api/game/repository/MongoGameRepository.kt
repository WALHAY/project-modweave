package git.walhay.modweave.api.game.repository

import git.walhay.modweave.api.game.Game
import git.walhay.modweave.api.game.GameId
import org.springframework.context.annotation.Profile
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Document(collection = "games")
data class GameDocument(
    @Id val id: String,
    val name: String,
    val description: String? = null,
    val imagePath: String,
)

interface SpringDataMongoGameRepository : MongoRepository<GameDocument, String> {
  fun findAllByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<GameDocument>
}

@Repository
@Profile("mongodb")
class MongoGameRepository(private val repository: SpringDataMongoGameRepository) :
    git.walhay.modweave.api.game.repository.GameRepository {
  private fun GameDocument.toDomain(): Game =
      Game(GameId(this.id), this.name, this.description, this.imagePath, mutableListOf())

  private fun Game.toDocument(): GameDocument =
      GameDocument(this.id.value, this.name, this.description, this.imagePath)

  override fun findById(gameId: GameId): Game? =
      repository.findById(gameId.value).map { it.toDomain() }.orElse(null)

  override fun findAll(pageable: Pageable): Page<Game> =
      repository.findAll(pageable).map { it.toDomain() }

  override fun findAll(name: String, pageable: Pageable): Page<Game> =
      repository.findAllByNameContainingIgnoreCase(name, pageable).map { it.toDomain() }

  override fun existsByIdIgnoreCase(gameId: GameId): Boolean = repository.existsById(gameId.value)

  override fun save(game: Game): Game = repository.save(game.toDocument()).toDomain()

  override fun deleteById(gameId: GameId) = repository.deleteById(gameId.value)
}
