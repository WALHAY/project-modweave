package git.walhay.modweave.api.game

import git.walhay.modweave.api.game.dto.AddGameDto
import git.walhay.modweave.api.game.dto.GameDto
import git.walhay.modweave.api.game.exception.GameExistsException
import git.walhay.modweave.api.game.exception.GameNotFoundException
import git.walhay.modweave.api.service.SimpleStorageService
import git.walhay.modweave.util.spinalCase
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
          .orElseThrow { GameNotFoundException("Game with id=$modId not found") }

  fun findGamesWithFilter(page: Int, size: Int, name: String?, sort: Sort): Page<Game> {
    val pageRequest = PageRequest.of(page, size, sort)
    if (name == null) {
      return gameRepository.findAll(pageRequest)
    }
    return gameRepository.findAllByNameContainingIgnoreCase(name, pageRequest)
  }

  fun uploadGame(dto: AddGameDto): Game {
    // TODO: add image path
    if (gameRepository.existsById(dto.name.spinalCase())) {
      throw GameExistsException("Game with id=${dto.name.spinalCase()} already exist")
    }

    val imagePath = "${dto.name}/logo.${FilenameUtils.getExtension(dto.image.originalFilename)}"

    val game =
        Game(dto.name, dto.description, simpleStorageService.uploadImage(imagePath, dto.image))

    return gameRepository.save(game)
  }
}
