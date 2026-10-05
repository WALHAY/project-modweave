package git.walhay.modweave.regression

import git.walhay.modweave.api.storage.S3ObjectStorage
import io.minio.*
import git.walhay.modweave.api.storage.StorageBucket
import java.io.ByteArrayInputStream
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.mock.web.MockMultipartFile

class StorageServiceTest {
  @Test
  fun `existing file bucket becomes private while images remain public`() {
    val client =
        mock(MinioClient::class.java) { invocation ->
          if (invocation.method.name == "bucketExists") true else null
        }
    S3ObjectStorage(client, "mods", "images").initBuckets()
    val policies =
        mockingDetails(client)
            .invocations
            .filter { it.method.name == "setBucketPolicy" }
            .map { it.arguments[0] as SetBucketPolicyArgs }
            .associate { it.bucket() to it.config() }
    assertFalse(policies.containsKey("mods"))
    val removed = mockingDetails(client).invocations.single { it.method.name == "deleteBucketPolicy" }.arguments[0] as DeleteBucketPolicyArgs
    assertEquals("mods", removed.bucket())
    assertTrue(policies["images"]!!.contains("s3:GetObject"))
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
