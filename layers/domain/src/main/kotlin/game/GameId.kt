package git.walhay.modweave.domain.game

import java.io.Serializable

@JvmInline
value class GameId(
    val value: String = "",
) : Serializable {
  override fun toString(): String = value
}
