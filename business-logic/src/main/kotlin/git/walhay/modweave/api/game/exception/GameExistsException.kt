package git.walhay.modweave.api.game.exception

import git.walhay.modweave.api.game.GameId

class GameExistsException(
    gameId: GameId,
) : Exception("Game with id=\"$gameId\" already exists")
