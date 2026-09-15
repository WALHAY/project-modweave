package git.walhay.modweave.service

import git.walhay.modweave.api.collection.*
import git.walhay.modweave.api.collection.command.CollectionCreateCommand
import git.walhay.modweave.api.collection.exception.CollectionNotFoundException
import git.walhay.modweave.api.collection.repository.CollectionRepository
import git.walhay.modweave.api.mod.*
import git.walhay.modweave.api.user.*
import git.walhay.modweave.testutils.TestFixtures
import java.util.UUID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class CollectionServiceTest : ServiceTestSupport() {
  private val repository = mock<CollectionRepository>()
  private val mods = mock<IModService>()
  private val service = CollectionService(repository, pagePolicy, mods)

  @Test
  fun `gets collection or throws`() {
    val collection = TestFixtures.collection()
    whenever(repository.findById(collection.id)).thenReturn(collection)
    assertSame(collection, service.getCollectionById(collection.id))

    val missingId = CollectionId(UUID.randomUUID())
    whenever(repository.findById(missingId)).thenReturn(null)
    assertThrows(CollectionNotFoundException::class.java) { service.getCollectionById(missingId) }
  }

  @Test
  fun `creates collection`() {
    whenever(repository.save(any())).thenAnswer { it.arguments[0] }

    val result =
        service.createCollection(UserId("alice"), CollectionCreateCommand("Favorites", null))

    assertEquals("Favorites", result.name)
    assertEquals(UserId("alice"), result.owner)
    verify(repository).save(any())
  }

  @Test
  fun `adds mod to collection`() {
    val collection = TestFixtures.collection()
    val mod = TestFixtures.mod()
    whenever(repository.findById(collection.id)).thenReturn(collection)
    whenever(mods.findModById(mod.id)).thenReturn(mod)
    whenever(repository.save(collection)).thenReturn(collection)

    val result = service.addModToCollection(UserId("alice"), collection.id, mod.id, null)

    assertTrue(result.mods.contains(mod))
    verify(repository).save(collection)
  }

  @Test
  fun `finds collections of user`() {
    val page = PageImpl(listOf(TestFixtures.collection()))
    whenever(repository.findAllByUser(any<UserId>(), any<Pageable>())).thenReturn(page)

    assertSame(page, service.findCollectionsOfUser(UserId("alice"), 0, 10, sort))
    verify(repository).findAllByUser(any<UserId>(), any<Pageable>())
  }

  @Test
  fun `deletes collection`() {
    val id = CollectionId(UUID.randomUUID())

    service.deleteCollection(UserId("alice"), id)

    verify(repository).deleteById(id)
  }

  @Test
  fun `delete mod from collection is currently a no-op`() {
    assertDoesNotThrow {
      service.deleteModFromCollection(
          UserId("alice"), CollectionId(UUID.randomUUID()), ModId("sodium"))
    }
  }
}
