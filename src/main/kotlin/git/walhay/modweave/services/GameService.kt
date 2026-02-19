package git.walhay.modweave.services

import git.walhay.modweave.dto.AddGameDTO
import git.walhay.modweave.models.Game
import git.walhay.modweave.repositories.GameRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class GameService @Autowired constructor(private val gameRepository: GameRepository) {

  fun addNewGame(addGame: AddGameDTO): Game {
    val game = Game(addGame.name, addGame.description)

    return gameRepository.save(game)
  }
}
