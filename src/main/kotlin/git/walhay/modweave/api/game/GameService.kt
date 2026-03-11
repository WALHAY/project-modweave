package git.walhay.modweave.api.game

import git.walhay.modweave.api.game.dto.AddGameDto
import git.walhay.modweave.api.game.exception.GameExistsException
import git.walhay.modweave.api.game.exception.GameNotFoundException
import git.walhay.modweave.api.game.repository.GameRepository
import git.walhay.modweave.api.service.SimpleStorageService
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
    private val simpleStorageService: SimpleStorageService
) {
  fun findGameById(modId: String): Game =
      gameRepository
          .findById(modId)
          ?: throw GameNotFoundException("Game with id=$modId not found")

  fun findGamesWithFilter(page: Int, size: Int, name: String?, sort: Sort): Page<Game> {
    val pageRequest = PageRequest.of(page, size, sort)
    if (name == null) {
      return gameRepository.findAll(pageRequest)
    }
    return gameRepository.findAll(name, pageRequest)
  }

  fun uploadGame(dto: AddGameDto): Game {
    if (gameRepository.existsById(dto.nameSpinal)) {
      throw GameExistsException("Game with id=${dto.nameSpinal} already exist")
    }

      var game=
          Game(dto.name, dto.description)
      game = gameRepository.save(game)

      game.imagePath = simpleStorageService.uploadImage("${game.name}/logo.${FilenameUtils.getExtension(dto.image.originalFilename)}", dto.image)

    return gameRepository.save(game)
  }
}
