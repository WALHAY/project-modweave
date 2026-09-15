package git.walhay.modweave.api.version.repository

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

class JpaVersionRepositoryTest {
  private val springData = mock<SpringDataVersionRepository>()
  private val repository = JpaVersionRepository(springData)

  @StateTransitionTest
  @Test
  fun `saves version and maps entity`() {
    val version = Version("1.0.0", "Initial release", ModId("sodium"))
    whenever(springData.save(any<VersionEntity>())).thenReturn(VersionEntity.fromVersion(version))

    assertEquals(version, repository.save(version))
    verify(springData).save(any<VersionEntity>())
  }

  @BoundaryConditionTest
  @Test
  fun `finds version by id through spring data`() {
    val version = Version("1.0.0", "Initial release", ModId("sodium"))
    whenever(springData.findById(version.id.value))
        .thenReturn(java.util.Optional.of(VersionEntity.fromVersion(version)))

    assertEquals(version, repository.findVersionById(version.id))
    verify(springData).findById(version.id.value)
  }
}
