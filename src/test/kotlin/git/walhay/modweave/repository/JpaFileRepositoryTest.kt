package git.walhay.modweave.api.file.repository

import git.walhay.modweave.api.file.File
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.version.Version
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@Tag("interaction")
class JpaFileRepositoryTest {
  private val springData = mock<SpringDataFileRepository>()
  private val repository = JpaFileRepository(springData)

  @Test
  fun `finds file by path`() {
    val file = file()
    whenever(springData.findByFilePath(file.filePath)).thenReturn(FileEntity.fromFile(file))

    assertEquals(file, repository.findByFilePath(file.filePath))
    verify(springData).findByFilePath(file.filePath)
  }

  @Test
  fun `saves file and maps entity`() {
    val file = file()
    whenever(springData.save(any<FileEntity>())).thenReturn(FileEntity.fromFile(file))

    assertEquals(file, repository.save(file))
    verify(springData).save(any<FileEntity>())
  }

  private fun file() = File("mod.zip", "mods/mod.zip", Version("1.0.0", modId = ModId("sodium")).id)
}
