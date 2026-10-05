package git.walhay.modweave.testutils

import java.time.Duration
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.MountableFile

object StorageTestInfrastructure {
  const val S3_IMAGE =
      "chrislusf/seaweedfs@sha256:4e61d15fd35994cb1e43e1e553dff106794841fd9a99ade2fc8c8bfce4d7872d"

  val s3 by lazy {
    GenericContainer(S3_IMAGE)
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
        .apply { start() }
  }

  val postgres by lazy {
    PostgreSQLContainer("postgres:16-alpine").apply {
      withCopyFileToContainer(
          MountableFile.forClasspathResource("init.sql"), "/docker-entrypoint-initdb.d/01-init.sql")
      withCopyFileToContainer(
          MountableFile.forClasspathResource("trigger.sql"),
          "/docker-entrypoint-initdb.d/02-trigger.sql")
      withCopyFileToContainer(
          MountableFile.forClasspathResource("storage-cleanup.sql"),
          "/docker-entrypoint-initdb.d/03-storage-cleanup.sql")
      start()
    }
  }

  val endpoint: String
    get() = "http://${s3.host}:${s3.getMappedPort(8333)}"
}
