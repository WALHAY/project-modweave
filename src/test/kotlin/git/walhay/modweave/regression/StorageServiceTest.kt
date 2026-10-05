package git.walhay.modweave.regression

import git.walhay.modweave.api.storage.S3ObjectStorage
import git.walhay.modweave.api.storage.StorageBucket
import io.minio.*
import java.io.ByteArrayInputStream
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.mock.web.MockMultipartFile

class StorageServiceTest {
  @Test
  fun `bucket initialization checks both configured buckets`() {
    val client =
        mock(MinioClient::class.java) { invocation ->
          if (invocation.method.name == "bucketExists") true else null
        }
    S3ObjectStorage(client, "mods", "images").initBuckets()
    val buckets =
        mockingDetails(client)
            .invocations
            .filter { it.method.name == "bucketExists" }
            .map { (it.arguments[0] as BucketExistsArgs).bucket() }
    assertEquals(listOf("mods", "images"), buckets)
    assertTrue(mockingDetails(client).invocations.none { it.method.name.endsWith("BucketPolicy") })
  }

  @Test
  fun `image cleanup targets the image bucket`() {
    val client = mock(MinioClient::class.java)
    S3ObjectStorage(client, "mods", "images").remove(StorageBucket.IMAGES, "games/logo.png")
    val args = mockingDetails(client).invocations.single().arguments[0] as RemoveObjectArgs
    assertEquals("images", args.bucket())
    assertEquals("games/logo.png", args.`object`())
  }

  @Test
  fun `upload input stream is closed when storage fails`() {
    var closed = false
    val input =
        object : ByteArrayInputStream(byteArrayOf(1)) {
          override fun close() {
            closed = true
            super.close()
          }
        }
    val file =
        object : MockMultipartFile("file", "file.zip", null, byteArrayOf(1)) {
          override fun getInputStream() = input
        }
    val client =
        mock(MinioClient::class.java) { throw IllegalStateException("storage unavailable") }
    assertThrows(IllegalStateException::class.java) {
      S3ObjectStorage(client, "mods", "images").upload(StorageBucket.MODS, "file.zip", file)
    }
    assertTrue(closed)
  }
}
