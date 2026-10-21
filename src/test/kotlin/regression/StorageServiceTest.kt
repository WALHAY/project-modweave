package git.walhay.modweave.regression

import git.walhay.modweave.persistence.storage.S3ObjectStorage
import git.walhay.modweave.persistence.storage.StorageBucket
import java.io.ByteArrayInputStream
import java.util.function.Consumer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.mock.web.MockMultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*

class StorageServiceTest {
  @Test
  fun `existing file bucket becomes private while images remain public`() {
    val client =
        mock(S3Client::class.java) { invocation ->
          if (invocation.arguments.firstOrNull() is Consumer<*>) invocation.callRealMethod()
          else null
        }
    S3ObjectStorage(client, "mods", "images").initBuckets()
    val policies =
        mockingDetails(client)
            .invocations
            .filter {
              it.method.name == "putBucketPolicy" && it.arguments[0] is PutBucketPolicyRequest
            }
            .map { it.arguments[0] as PutBucketPolicyRequest }
            .associate { it.bucket() to it.policy() }
    assertFalse(policies.containsKey("mods"))
    val removed =
        mockingDetails(client)
            .invocations
            .single {
              it.method.name == "deleteBucketPolicy" && it.arguments[0] is DeleteBucketPolicyRequest
            }
            .arguments[0]
            as DeleteBucketPolicyRequest
    assertEquals("mods", removed.bucket())
    assertTrue(policies["images"]!!.contains("s3:GetObject"))
  }

  @Test
  fun `image cleanup targets the image bucket`() {
    val client =
        mock(S3Client::class.java) { invocation ->
          if (invocation.arguments.firstOrNull() is Consumer<*>) invocation.callRealMethod()
          else null
        }
    S3ObjectStorage(client, "mods", "images").remove(StorageBucket.IMAGES, "games/logo.png")
    val args =
        mockingDetails(client)
            .invocations
            .single { it.arguments[0] is DeleteObjectRequest }
            .arguments[0]
            as DeleteObjectRequest
    assertEquals("images", args.bucket())
    assertEquals("games/logo.png", args.key())
  }

  @Test
  fun `upload retries reopen non-resettable multipart streams and close them all`() {
    val bytes = ByteArray(256 * 1024) { it.toByte() }
    var opened = 0
    var closed = 0
    val file =
        object : MockMultipartFile("file", "file.zip", null, bytes) {
          override fun getInputStream(): java.io.InputStream {
            opened++
            return object : ByteArrayInputStream(bytes) {
              override fun markSupported() = false

              override fun reset() {
                throw IllegalStateException("Stream cannot reset")
              }

              override fun close() {
                closed++
                super.close()
              }
            }
          }
        }
    val client =
        mock(S3Client::class.java) { invocation ->
          val body = invocation.arguments.getOrNull(1) as? RequestBody
          if (body != null) {
            repeat(2) {
              assertArrayEquals(bytes, body.contentStreamProvider().newStream().readAllBytes())
            }
            throw IllegalStateException("Upload failed after retry")
          }
          null
        }
    assertThrows(IllegalStateException::class.java) {
      S3ObjectStorage(client, "mods", "images").upload(StorageBucket.MODS, "file.zip", file)
    }
    assertEquals(2, opened)
    assertEquals(opened, closed)
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
        mock(S3Client::class.java) { invocation ->
          (invocation.arguments.getOrNull(1) as? RequestBody)?.contentStreamProvider()?.newStream()
          throw IllegalStateException("storage unavailable")
        }
    assertThrows(IllegalStateException::class.java) {
      S3ObjectStorage(client, "mods", "images").upload(StorageBucket.MODS, "file.zip", file)
    }
    assertTrue(closed)
  }
}
