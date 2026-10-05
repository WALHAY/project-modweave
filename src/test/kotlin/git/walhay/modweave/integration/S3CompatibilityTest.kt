package git.walhay.modweave.integration

import git.walhay.modweave.api.storage.S3ObjectStorage
import io.minio.MinioClient
import git.walhay.modweave.api.storage.StorageBucket
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.mock.web.MockMultipartFile
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.MountableFile
import java.time.Duration

class S3CompatibilityTest {
  @Test
  fun `free S3 container supports bucket policies uploads downloads and deletes`() {
    GenericContainer("chrislusf/seaweedfs@sha256:4e61d15fd35994cb1e43e1e553dff106794841fd9a99ade2fc8c8bfce4d7872d")
        .withExposedPorts(8333)
        .withCopyFileToContainer(MountableFile.forClasspathResource("storage/s3.json"), "/etc/seaweedfs/s3.json")
        .withCommand("server", "-dir=/data", "-s3", "-s3.port=8333", "-s3.config=/etc/seaweedfs/s3.json", "-ip.bind=0.0.0.0")
        .waitingFor(Wait.forListeningPort())
        .withStartupTimeout(Duration.ofMinutes(2))
        .use { container ->
          container.start()
          println("S3_IMAGE_DIGESTS=" + container.dockerClient.inspectImageCmd(container.dockerImageName).exec().repoDigests)
          println("S3_VERSION=" + container.execInContainer("weed", "version").stdout)
          val endpoint = "http://${container.host}:${container.getMappedPort(8333)}"
          val client = MinioClient.builder().endpoint(endpoint).credentials("username", "password").build()
          val storage = S3ObjectStorage(client, "mods", "images")
          storage.initBuckets()
          val file = MockMultipartFile("files", "example.zip", "application/octet-stream", byteArrayOf(1, 2, 3))
          storage.upload(StorageBucket.MODS, "example.zip", file)
          assertArrayEquals(byteArrayOf(1, 2, 3), storage.downloadVersionFile("example.zip").use { it.readAllBytes() })
          storage.remove(StorageBucket.MODS, "example.zip")
          assertThrows(io.minio.errors.ErrorResponseException::class.java) { storage.downloadVersionFile("example.zip") }
        }
  }
}
