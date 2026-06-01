package git.walhay.modweave.api.game

import git.walhay.modweave.api.common.paging.PageSizePolicy
import git.walhay.modweave.api.game.command.GameCreateCommand
import git.walhay.modweave.api.game.exception.GameExistsException
import git.walhay.modweave.api.game.exception.GameNotFoundException
import git.walhay.modweave.api.game.repository.GameRepository
import git.walhay.modweave.api.storage.ISimpleStorageService
import mu.KLogger
import mu.KotlinLogging
import org.apache.commons.io.FilenameUtils
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class GameService(
    private val gameRepository: GameRepository,
    private val simpleStorageService: ISimpleStorageService,
    private val pageSizePolicy: PageSizePolicy,
) : IGameService {
  private val logger: KLogger = KotlinLogging.logger {}

  @Cacheable("games", key = "#gameId")
  override fun findGameById(gameId: GameId): Game {
    logger.debug { "Fetching game by id: $gameId" }
    return gameRepository.findById(gameId) ?: throw GameNotFoundException(gameId)
  }

  override fun findGamesWithFilter(
      page: Int,
      size: Int,
      name: String?,
      sort: Sort,
  ): Page<Game> {
    logger.debug {
      "Fetching games with filter - page: $page, size: $size, name: $name, sort: $sort"
    }
    val pageRequest = PageRequest.of(page, pageSizePolicy.normalize(size), sort)
    if (name == null) {
      return gameRepository.findAll(pageRequest)
    }
    return gameRepository.findAll(name, pageRequest)
  }

  @CachePut("games", key = "#result.id")
  @PreAuthorize("@accessSecurity.isAdmin()")
  override fun uploadGame(command: GameCreateCommand): Game {
    logger.info { "Creating new game: ${command.id}" }
    if (gameRepository.existsByIdIgnoreCase(command.id)) {
      logger.warn { "Game creation failed - game already exists: ${command.id}" }
      throw GameExistsException(command.id)
    }

    val game =
        command
            .let { (id, name, description) -> Game(id, name, description) }
            .let { gameRepository.save(it) }

    logger.debug { "Uploading game image for: ${game.name}" }
    game.imagePath =
        simpleStorageService.uploadImage(
            "${game.name}/logo.${FilenameUtils.getExtension(command.image.originalFilename)}",
            command.image,
        )

    val savedGame = gameRepository.save(game)
    logger.info { "Game created successfully: ${savedGame.id}" }
    return savedGame
  }

  @CacheEvict("games", key = "#gameId.value")
  @PreAuthorize("@accessSecurity.isAdmin()")
  override fun deleteGame(gameId: GameId) {
    logger.info { "Deleting game: $gameId" }
    gameRepository.deleteById(gameId)
    logger.info { "Game deleted successfully: $gameId" }
  }
}
