package git.walhay.modweave.service

import git.walhay.modweave.api.collection.*
import git.walhay.modweave.api.collection.command.CollectionCreateCommand
import git.walhay.modweave.api.collection.exception.CollectionNotFoundException
import git.walhay.modweave.api.collection.repository.CollectionRepository
import git.walhay.modweave.api.mod.*
import git.walhay.modweave.api.user.*
import git.walhay.modweave.testutils.InteractionTest
import git.walhay.modweave.testutils.TestFixtures
import java.util.UUID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

@InteractionTest
class CollectionServiceTest : ServiceTestSupport() {
  private val repository = mock<CollectionRepository>()
  private val mods = mock<IModService>()
  private val service = CollectionService(repository, pagePolicy, mods)

  @Test
  fun `gets existing collection`() {
    val collection = TestFixtures.collection()
    whenever(repository.findById(collection.id)).thenReturn(collection)

    assertSame(collection, service.getCollectionById(collection.id))
  }

  @Test
  fun `throws when collection is missing`() {
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
  fun `rejects adding mod to missing collection`() {
    val collectionId = CollectionId(UUID.randomUUID())
    whenever(repository.findById(collectionId)).thenReturn(null)

    assertThrows(CollectionNotFoundException::class.java) {
      service.addModToCollection(UserId("alice"), collectionId, ModId("missing"), null)
    }
    verify(mods, never()).findModById(any())
  }

  @Test
  fun `propagates missing mod when adding to collection`() {
    val collection = TestFixtures.collection()
    val modId = ModId("missing")
    whenever(repository.findById(collection.id)).thenReturn(collection)
    whenever(mods.findModById(modId)).thenThrow(IllegalStateException("mod missing"))

    assertThrows(IllegalStateException::class.java) {
      service.addModToCollection(UserId("alice"), collection.id, modId, null)
    }
    verify(repository, never()).save(any())
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
  fun `propagates collection delete failure`() {
    val id = CollectionId(UUID.randomUUID())
    doThrow(IllegalStateException("database")).whenever(repository).deleteById(id)

    assertThrows(IllegalStateException::class.java) {
      service.deleteCollection(UserId("alice"), id)
    }
  }

  @Test
  fun `deletes mod from collection`() {
    val mod = TestFixtures.mod()
    val collection = TestFixtures.collection().copy(mods = mutableListOf(mod))
    whenever(repository.findById(collection.id)).thenReturn(collection)
    whenever(repository.save(collection)).thenReturn(collection)

    service.deleteModFromCollection(UserId("alice"), collection.id, mod.id)

    assertTrue(collection.mods.isEmpty())
    verify(repository).save(collection)
  }

  @Test
  fun `rejects deleting mod absent from collection`() {
    val collection = TestFixtures.collection()
    whenever(repository.findById(collection.id)).thenReturn(collection)

    assertThrows(IllegalArgumentException::class.java) {
      service.deleteModFromCollection(UserId("alice"), collection.id, ModId("missing"))
    }
    verify(repository, never()).save(any())
  }
}
