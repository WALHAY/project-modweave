package git.walhay.modweave.domain.game

class GameExistsException(
    gameId: GameId,
) : RuntimeException("Game with id=\"$gameId\" already exists")
