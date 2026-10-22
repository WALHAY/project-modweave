package git.walhay.modweave.persistence.game

import git.walhay.modweave.domain.common.paging.Page
import git.walhay.modweave.domain.common.paging.PageRequest
import git.walhay.modweave.domain.game.Game
import git.walhay.modweave.domain.game.GameId
import git.walhay.modweave.domain.game.GameRepository
import git.walhay.modweave.persistence.common.paging.toDomainPage
import git.walhay.modweave.persistence.common.paging.toSpringPageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class JpaGameRepository(
    private val repository: SpringDataGameRepository,
) : GameRepository {
  override fun findById(gameId: GameId): Game? = repository.findByIdOrNull(gameId.value)?.toDomain()

  override fun findAll(pageRequest: PageRequest): Page<Game> =
      repository.findAll(pageRequest.toSpringPageable()).toDomainPage { it.toDomain() }

  override fun findAll(
      name: String,
      pageRequest: PageRequest,
  ): Page<Game> =
      repository
          .findAllByNameContainingIgnoreCase(name, pageRequest.toSpringPageable())
          .toDomainPage { it.toDomain() }

  override fun existsByIdIgnoreCase(gameId: GameId): Boolean =
      repository.existsByIdIgnoreCase(gameId.value)

  override fun save(game: Game): Game =
      repository.save<GameEntity>(GameEntity.fromGame(game)).toDomain()

  override fun deleteById(gameId: GameId) = repository.deleteById(gameId.value)
}
