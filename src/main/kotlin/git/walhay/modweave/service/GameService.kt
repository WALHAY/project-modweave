package git.walhay.modweave.service

import git.walhay.modweave.dto.AddGameDTO
import git.walhay.modweave.model.Game
import git.walhay.modweave.repository.GameRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class GameService @Autowired constructor(private val gameRepository: GameRepository) {

  fun addNewGame(addGame: AddGameDTO): Game {
      // TODO: add image path
    val game = Game(addGame.name, addGame.description, "")

    return gameRepository.save(game)
  }
}
