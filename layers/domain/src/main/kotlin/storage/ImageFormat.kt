package git.walhay.modweave.domain.storage

import java.io.InputStream
import javax.imageio.ImageIO
import javax.imageio.stream.MemoryCacheImageInputStream

/** Inspect dimensions before decoding; multipart byte limits do not bound decoded memory. */
enum class ImageFormat(val contentType: String, val extensions: Set<String>) {
  PNG("image/png", setOf("png")),
  JPEG("image/jpeg", setOf("jpg", "jpeg"));

  companion object {
    const val MAX_DIMENSION = 4096
    const val MAX_PIXELS = 16_000_000L

    fun detect(input: InputStream): ImageFormat {
      val header = input.readNBytes(8)
      return when {
        header.contentEquals(
            byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a)) -> PNG
        header.size >= 3 &&
            header[0] == 0xff.toByte() &&
            header[1] == 0xd8.toByte() &&
            header[2] == 0xff.toByte() -> JPEG
        else -> throw IllegalArgumentException("Only PNG or JPEG images are allowed")
      }
    }

    fun inspect(input: InputStream, decode: Boolean = false): ImageFormat =
        MemoryCacheImageInputStream(input).use { image ->
          val readers = ImageIO.getImageReaders(image)
          require(readers.hasNext()) { "Only PNG or JPEG images are allowed" }
          val reader = readers.next()
          try {
            val format =
                entries.firstOrNull { it.name.equals(reader.formatName, ignoreCase = true) }
            requireNotNull(format) { "Only PNG or JPEG images are allowed" }
            reader.input = image
            val width = reader.getWidth(0)
            val height = reader.getHeight(0)
            require(
                width in 1..MAX_DIMENSION &&
                    height in 1..MAX_DIMENSION &&
                    width.toLong() * height <= MAX_PIXELS) {
                  "Image exceeds 4096 pixels per side or 16 megapixels"
                }
            if (decode) require(reader.read(0) != null) { "Invalid image contents" }
            format
          } finally {
            reader.dispose()
          }
        }
  }
}

class ImageNotFoundException : RuntimeException("Image not found")
