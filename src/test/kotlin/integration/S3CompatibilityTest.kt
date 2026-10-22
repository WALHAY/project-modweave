package git.walhay.modweave.integration

import git.walhay.modweave.persistence.config.DataConfiguration
import git.walhay.modweave.persistence.storage.S3ObjectStorage
import git.walhay.modweave.persistence.storage.StorageBucket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.MountableFile
import software.amazon.awssdk.services.s3.model.NoSuchKeyException

class S3CompatibilityTest {
  @Test
  fun `free S3 container supports bucket policies uploads downloads and deletes`() {
    GenericContainer(
            "chrislusf/seaweedfs@sha256:4e61d15fd35994cb1e43e1e553dff106794841fd9a99ade2fc8c8bfce4d7872d")
        .withExposedPorts(8333)
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("storage/s3.json"), "/etc/seaweedfs/s3.json")
        .withCommand(
            "server",
            "-dir=/data",
            "-s3",
            "-s3.port=8333",
            "-s3.config=/etc/seaweedfs/s3.json",
            "-ip.bind=0.0.0.0")
        .waitingFor(Wait.forListeningPort())
        .withStartupTimeout(Duration.ofMinutes(2))
        .use { container ->
          container.start()
          println(
              "S3_IMAGE_DIGESTS=" +
                  container.dockerClient
                      .inspectImageCmd(container.dockerImageName)
                      .exec()
                      .repoDigests)
          println("S3_VERSION=" + container.execInContainer("weed", "version").stdout)
          val endpoint = "http://${container.host}:${container.getMappedPort(8333)}"
          DataConfiguration()
              .s3Client(URI.create(endpoint), "username", "password", "us-east-1")
              .use { client ->
                val storage = S3ObjectStorage(client, "mods", "images")
                // Simulate an existing public image bucket before the application upgrade.
                client.createBucket { it.bucket("images") }
                client.putBucketPolicy {
                  it.bucket("images")
                      .policy(
                          """{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":"*","Action":["s3:GetObject"],"Resource":["arn:aws:s3:::images/*"]}]}""")
                }
                storage.initBuckets()
                // Large enough to catch checksum/trailer issues and accidental truncation.
                val bytes = ByteArray(256 * 1024) { it.toByte() }
                val key = "mods/пример + mod/logo.png"
                val file =
                    MockMultipartFile("files", "example.zip", "application/octet-stream", bytes)
                storage.upload(StorageBucket.MODS, key, file)
                assertArrayEquals(bytes, storage.downloadVersionFile(key).use { it.readAllBytes() })
                val png =
                    java.io
                        .ByteArrayOutputStream()
                        .also {
                          javax.imageio.ImageIO.write(
                              java.awt.image.BufferedImage(
                                  1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB),
                              "png",
                              it)
                        }
                        .toByteArray()
                storage.upload(
                    StorageBucket.IMAGES,
                    key,
                    MockMultipartFile("image", "logo.png", "text/plain", png))
                assertEquals(
                    "image/png", client.headObject { it.bucket("images").key(key) }.contentType())
                assertArrayEquals(png, storage.downloadImage(key).use { it.readAllBytes() })
                val http = HttpClient.newHttpClient()
                fun anonymous(bucket: String) =
                    http.send(
                        HttpRequest.newBuilder(
                                URI.create(
                                    endpoint +
                                        "/" +
                                        bucket +
                                        "/" +
                                        java.net.URLEncoder.encode(key, Charsets.UTF_8)
                                            .replace("+", "%20")
                                            .replace("%2F", "/")))
                            .build(),
                        HttpResponse.BodyHandlers.ofByteArray())
                assertEquals(403, anonymous("mods").statusCode())
                val image = anonymous("images")
                assertEquals(403, image.statusCode())
                storage.initBuckets() // Existing buckets and absent policies are also supported.
                storage.remove(StorageBucket.MODS, key)
                storage.remove(StorageBucket.MODS, key) // Cleanup may repeat after a lost response.
                storage.remove(StorageBucket.IMAGES, key)
                assertThrows(NoSuchKeyException::class.java) { storage.downloadVersionFile(key) }
                assertThrows(
                    git.walhay.modweave.domain.storage.ImageNotFoundException::class.java) {
                      storage.downloadImage(key)
                    }
              }
        }
  }
}
