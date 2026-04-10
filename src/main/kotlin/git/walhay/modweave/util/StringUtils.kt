package git.walhay.modweave.util

fun String.spinalCase(): String {
  val stringBuilder = StringBuilder()

  for (c in this) {
    when {
      c.isLetterOrDigit() -> stringBuilder.append(c.lowercase())
      c.isWhitespace() -> stringBuilder.append('-')
    }
  }

  return stringBuilder.toString()
}
