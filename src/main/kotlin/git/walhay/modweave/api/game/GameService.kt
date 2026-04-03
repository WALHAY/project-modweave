package git.walhay.modweave.api.game

import git.walhay.modweave.api.game.dto.GameUploadDto
import git.walhay.modweave.api.game.exception.GameExistsException
import git.walhay.modweave.api.game.exception.GameNotFoundException
import git.walhay.modweave.api.game.repository.GameRepository
import git.walhay.modweave.api.storage.ISimpleStorageService
import org.apache.commons.io.FilenameUtils
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
  override fun findGameById(gameId: GameId): Game =
      gameRepository.findById(gameId) ?: throw GameNotFoundException(gameId)

  override fun findGamesWithFilter(page: Int, size: Int, name: String?, sort: Sort): Page<Game> {
    val pageRequest = PageRequest.of(page, size, sort)
    if (name == null) {
      return gameRepository.findAll(pageRequest)
    }
    return gameRepository.findAll(name, pageRequest)
  }

  override fun uploadGame(dto: GameUploadDto): Game {
    if (gameRepository.existsById(dto.nameSpinal)) {
      throw GameExistsException(dto.nameSpinal)
    }

    var game = Game(dto.name, dto.description)
    game = gameRepository.save(game)

    game.imagePath =
        simpleStorageService.uploadImage(
            "${game.name}/logo.${FilenameUtils.getExtension(dto.image.originalFilename)}",
            dto.image)

    return gameRepository.save(game)
  }
}
