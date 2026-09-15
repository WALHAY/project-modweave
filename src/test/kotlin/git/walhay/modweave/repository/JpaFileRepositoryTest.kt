package git.walhay.modweave.api.file.repository

import git.walhay.modweave.api.file.File
import git.walhay.modweave.api.mod.ModId
import git.walhay.modweave.api.version.Version
import git.walhay.modweave.testutils.BoundaryConditionTest
import git.walhay.modweave.testutils.StateTransitionTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class JpaFileRepositoryTest {
  private val springData = mock<SpringDataFileRepository>()
  private val repository = JpaFileRepository(springData)

  @BoundaryConditionTest
  @Test
  fun `finds file by path`() {
    val file = file()
    whenever(springData.findByFilePath(file.filePath)).thenReturn(FileEntity.fromFile(file))

    assertEquals(file, repository.findByFilePath(file.filePath))
    verify(springData).findByFilePath(file.filePath)
  }

  @StateTransitionTest
  @Test
  fun `saves file and maps entity`() {
    val file = file()
    whenever(springData.save(any<FileEntity>())).thenReturn(FileEntity.fromFile(file))

    assertEquals(file, repository.save(file))
    verify(springData).save(any<FileEntity>())
  }

  private fun file() = File("mod.zip", "mods/mod.zip", Version("1.0.0", modId = ModId("sodium")).id)
}
