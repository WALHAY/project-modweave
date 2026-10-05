package git.walhay.modweave.api.game

import git.walhay.modweave.api.common.paging.Page
import git.walhay.modweave.api.common.paging.PageSizePolicy
import git.walhay.modweave.api.game.command.GameCreateCommand
import git.walhay.modweave.api.game.exception.GameExistsException
import git.walhay.modweave.api.game.exception.GameNotFoundException
import git.walhay.modweave.api.game.repository.GameRepository
import git.walhay.modweave.api.storage.ISimpleStorageService
import java.util.UUID
import mu.KLogger
import mu.KotlinLogging
import org.apache.commons.io.FilenameUtils
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(rollbackFor = [Exception::class])
class GameService(
    private val gameRepository: GameRepository,
    private val simpleStorageService: ISimpleStorageService,
    private val pageSizePolicy: PageSizePolicy,
) : IGameService {
  private val logger: KLogger = KotlinLogging.logger {}

  @Cacheable("games", key = "#p0")
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

  @CacheEvict(value = ["mods", "games", "users"], allEntries = true)
  @PreAuthorize("@accessSecurity.isAdmin()")
  override fun uploadGame(command: GameCreateCommand): Game {
    logger.info { "Creating new game: ${command.id}" }
    if (gameRepository.existsByIdIgnoreCase(command.id)) {
      logger.warn { "Game creation failed - game already exists: ${command.id}" }
      throw GameExistsException(command.id)
    }

    val imagePath =
        "games/${command.id.value}/${UUID.randomUUID()}/logo.${FilenameUtils.getExtension(command.image.originalFilename)}"
    val storedImage = simpleStorageService.uploadImage(imagePath, command.image)
    return gameRepository.save(Game(command.id, command.name, command.description, storedImage))
  }

  @CacheEvict(
      value = ["mods", "versions", "games", "collections", "users", "comments"], allEntries = true)
  @PreAuthorize("@accessSecurity.isAdmin()")
  override fun deleteGame(gameId: GameId): Unit {
    logger.info { "Deleting game: $gameId" }
    val game = findGameById(gameId)
    gameRepository.deleteById(gameId)
    game.mods.forEach { mod ->
      mod.versions
          .flatMap { it.files }
          .forEach { simpleStorageService.removeVersionFile(it.filePath) }
      simpleStorageService.removeImage(mod.imagePath)
    }
    simpleStorageService.removeImage(game.imagePath)
    logger.info { "Game deleted successfully: $gameId" }
  }
}
