package git.walhay.modweave.service

import git.walhay.modweave.api.file.FileService
import git.walhay.modweave.api.file.repository.FileRepository
import git.walhay.modweave.api.storage.ISimpleStorageService
import git.walhay.modweave.testutils.TestFixtures
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class FileServiceTest {
  @Test
  fun `uploads and saves every version file`() {
    val storage = mock<ISimpleStorageService>()
    val repository = mock<FileRepository>()
    val service = FileService(storage, repository)
    val version = TestFixtures.version()
    val first = TestFixtures.archive("one.zip")
    val second = TestFixtures.archive("two.zip")
    whenever(storage.uploadVersionFile(any(), any())).thenAnswer { it.arguments[0] as String }
    whenever(repository.save(any())).thenAnswer { it.arguments[0] }

    service.uploadVersionFiles(version, listOf(first, second))

    assertEquals(2, version.files.size)
    verify(repository, times(2)).save(any())
    verify(storage).uploadVersionFile("sodium/1.0.0/one.zip", first)
    verify(storage).uploadVersionFile("sodium/1.0.0/two.zip", second)
  }

  @Test
  fun `removes already uploaded files after failure`() {
    val storage = mock<ISimpleStorageService>()
    val repository = mock<FileRepository>()
    val service = FileService(storage, repository)
    val version = TestFixtures.version()
    val first = TestFixtures.archive("one.zip")
    val second = TestFixtures.archive("two.zip")
    whenever(storage.uploadVersionFile("sodium/1.0.0/one.zip", first)).thenReturn("remote-one")
    whenever(storage.uploadVersionFile("sodium/1.0.0/two.zip", second))
        .thenThrow(IllegalStateException("upload failed"))

    assertThrows(IllegalStateException::class.java) {
      service.uploadVersionFiles(version, listOf(first, second))
    }
    verify(storage).removeVersionFile("remote-one")
  }

  @Test
  fun `does not save files when input is empty`() {
    val storage = mock<ISimpleStorageService>()
    val repository = mock<FileRepository>()
    val service = FileService(storage, repository)

    service.uploadVersionFiles(TestFixtures.version(), emptyList())

    verifyNoInteractions(storage, repository)
  }
}
