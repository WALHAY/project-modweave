package git.walhay.modweave.api.game

@JvmInline
value class GameId(
    val value: String = "",
) {
  override fun toString(): String = value
}
