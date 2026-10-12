package git.walhay.modweave.domain.game

class GameNotFoundException(
    gameId: GameId,
) : RuntimeException("Game with id=\"$gameId\" not found")
