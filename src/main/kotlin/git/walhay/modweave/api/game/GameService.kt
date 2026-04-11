package git.walhay.modweave.api.game

import git.walhay.modweave.api.game.command.GameCreateCommand
import git.walhay.modweave.api.game.exception.GameExistsException
import git.walhay.modweave.api.game.exception.GameNotFoundException
import git.walhay.modweave.api.game.repository.GameRepository
import git.walhay.modweave.api.storage.ISimpleStorageService
import org.apache.commons.io.FilenameUtils
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class GameService(
    private val gameRepository: GameRepository,
    private val simpleStorageService: ISimpleStorageService
) : IGameService {
  @Cacheable("games", key = "#gameId.value")
  override fun findGameById(gameId: GameId): Game =
      gameRepository.findById(gameId) ?: throw GameNotFoundException(gameId)

  override fun findGamesWithFilter(page: Int, size: Int, name: String?, sort: Sort): Page<Game> {
    val pageRequest = PageRequest.of(page, size, sort)
    if (name == null) {
      return gameRepository.findAll(pageRequest)
    }
    return gameRepository.findAll(name, pageRequest)
  }

  @CachePut("games", key = "#result.id.value")
  override fun uploadGame(command: GameCreateCommand): Game {
    if (gameRepository.existsByIdIgnoreCase(command.id)) {
      throw GameExistsException(command.id)
    }

    val game =
        command
            .let { (id, name, description) -> Game(id, name, description) }
            .let { gameRepository.save(it) }

    game.imagePath =
        simpleStorageService.uploadImage(
            "${game.name}/logo.${FilenameUtils.getExtension(command.image.originalFilename)}",
            command.image)

    return gameRepository.save(game)
  }

  @CacheEvict("games", key = "#gameId.value")
  override fun deleteGame(gameId: GameId) = gameRepository.deleteById(gameId)
}
