package git.walhay.modweave.api.v1

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/games")
class GameController {

    @GetMapping
    fun getGames() {

    }

    @PostMapping
    fun addGame() {

    }
}