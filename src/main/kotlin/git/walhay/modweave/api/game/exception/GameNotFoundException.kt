package git.walhay.modweave.api.game.exception

import git.walhay.modweave.api.game.GameId

class GameNotFoundException(gameId: GameId) : Exception("Game with id=\"$gameId\" not found")
