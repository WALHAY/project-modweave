package git.walhay.modweave.regression

import git.walhay.modweave.api.collection.Collection
import git.walhay.modweave.api.collection.CollectionService
import git.walhay.modweave.api.collection.repository.CollectionRepository
import git.walhay.modweave.api.common.paging.PageSizePolicy
import git.walhay.modweave.api.file.FileService
import git.walhay.modweave.api.file.repository.FileRepository
import git.walhay.modweave.api.game.GameId
import git.walhay.modweave.api.mod.*
import git.walhay.modweave.api.storage.ISimpleStorageService
import git.walhay.modweave.api.user.UserId
import git.walhay.modweave.api.version.Version
import git.walhay.modweave.config.properties.ModweaveProperties
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.mock.web.MockMultipartFile

class CollectionAndUploadTest {
  private val owner = UserId("owner")

  private fun mod(id: String) =
      Mod(ModId(id), id, imagePath = "img", publisherId = owner, gameId = GameId("game"))

  @Test
  fun `insert at index and delete persist the expected collection order`() {
    val repository = mock(CollectionRepository::class.java)
    val mods = mock(IModService::class.java)
    val first = mod("first")
    val second = mod("second")
    val collection = Collection("favorites", null, owner, mutableListOf(first))
    `when`(repository.findById(collection.id)).thenReturn(collection)
    `when`(repository.save(collection)).thenAnswer { it.arguments[0] }
    `when`(mods.findModById(second.id)).thenReturn(second)
    val service = CollectionService(repository, PageSizePolicy(ModweaveProperties()), mods)
    service.addModToCollection(owner, collection.id, second.id, 0)
    assertEquals(listOf(second.id, first.id), collection.mods.map { it.id })
    assertThrows(IllegalArgumentException::class.java) {
      service.addModToCollection(owner, collection.id, second.id, null)
    }
    service.deleteModFromCollection(owner, collection.id, first.id)
    assertEquals(listOf(second.id), collection.mods.map { it.id })
    verify(repository, times(2)).save(collection)
  }

  @Test
  fun `invalid upload names and empty files are rejected before writing to storage`() {
    val storage = mock(ISimpleStorageService::class.java)
    val repository = mock(FileRepository::class.java)
    val service = FileService(storage, repository)
    val version = Version("1.0", null, ModId("mod"))
    for (file in
        listOf(
            MockMultipartFile("files", "../escape.zip", null, byteArrayOf(1)),
            MockMultipartFile("files", "empty.zip", null, byteArrayOf()))) {
      assertThrows(IllegalArgumentException::class.java) {
        service.uploadVersionFiles(version, listOf(file))
      }
    }
    verifyNoInteractions(storage, repository)
  }
}
