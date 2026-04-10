package git.walhay.modweave.api.game.repository

import git.walhay.modweave.api.game.Game
import git.walhay.modweave.api.game.GameId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class JpaGameRepository(private val repository: SpringDataGameRepository) : GameRepository {
  override fun findById(gameId: GameId): Game? = repository.findByIdOrNull(gameId.value)?.toDomain()

  override fun findAll(pageable: Pageable): Page<Game> =
      repository.findAll(pageable).map { it.toDomain() }

  override fun findAll(name: String, pageable: Pageable): Page<Game> =
      repository.findAllByNameContainingIgnoreCase(name, pageable).map { it.toDomain() }

  override fun existsByIdIgnoreCase(gameId: GameId): Boolean =
      repository.existsByIdIgnoreCase(gameId.value)

  override fun save(game: Game): Game =
      repository.save<GameEntity>(GameEntity.fromGame(game)).toDomain()
}
